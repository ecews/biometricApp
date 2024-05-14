package org.ecews.biometricapp.services;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.ecews.biometricapp.entities.NdrMessageLog;
import org.ecews.biometricapp.entities.NdrXmlStatus;
import org.ecews.biometricapp.entities.dtos.NDRErrorDTO;
import org.ecews.biometricapp.entities.dtos.NdrXmlStatusDto;
import org.ecews.biometricapp.entities.dtos.PatientDemographicDTO;
import org.ecews.biometricapp.entities.dtos.PatientDemographics;
import org.ecews.biometricapp.recapture.Container;
import org.ecews.biometricapp.repositories.NDRCodeSetRepository;
import org.ecews.biometricapp.repositories.NdrMessageLogRepository;
import org.ecews.biometricapp.repositories.NdrXmlStatusRepository;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.ecews.biometricapp.utils.ZipUtility;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
@RequiredArgsConstructor
@Slf4j
public class RecaptureBiometricService {
	
	private final RecaptureBiometricMapper biometricMapper;
	
	private final NdrMessageLogRepository ndrMessageLogRepository;
	
	private final NdrXmlStatusRepository ndrXmlStatusRepository;
	private final NDRCodeSetRepository nDRCodeSetRepository;

	private final NDRCodeSetResolverService ndrCodeSetResolverService;
	public static final String BASE_DIR = "runtime/ndr/transfer/";
	public boolean generateRecaptureBiometrics(Long facilityId, Integer recaptureType) {
		String pathname = BASE_DIR + "temp/biorecapture/" + facilityId + "/";
		cleanupFacility(facilityId, pathname);
		AtomicInteger count = new AtomicInteger(0);
		log.info("start generating recapture biometrics patients");
		String deduplicationType;
		if (recaptureType == 1) {
			deduplicationType = DeDuplicationConfigs.RECAPTURE_ONE_AND_BASELINE;
		} else if (recaptureType == 2) {
			deduplicationType = DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE;
		} else if (recaptureType == 3) {
			deduplicationType = DeDuplicationConfigs.RECAPTURE_THREE_AND_TWO;
		} else {
			throw new IllegalArgumentException("Invalid recaptureType: " + recaptureType);
		}
		Iterable<String> patientsIds =
				nDRCodeSetRepository.getRecapturedPatientIds(facilityId, recaptureType, deduplicationType.toString());

		log.info("About {} patients are identified for generating NDR file", patientsIds.iterator().hasNext());
		if (!patientsIds.iterator().hasNext()) {
			return false;
		}
		log.info("fetching patient demographics **** {}, ", patientsIds);
		List<PatientDemographicDTO> demographics = new ArrayList<>();
		patientsIds.forEach(id -> {
					Optional<PatientDemographicDTO> demographicsOptional =
							ndrXmlStatusRepository.getPatientDemographics(id,facilityId);
					demographicsOptional.ifPresent(d -> {
						try {
							Container container = biometricMapper.getRecaptureBiometricMapper(d,recaptureType);
							if (container != null) {
								JAXBContext jaxbContext = JAXBContext.newInstance(Container.class);
								Marshaller jaxbMarshaller = getMarshaller(jaxbContext);
								SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
								Schema schema = sf.newSchema(getClass().getClassLoader().getResource("NDR_FP_1.xsd"));
								jaxbMarshaller.setSchema(schema);
								//creating file
								File dir = new File(pathname);
								if (!dir.exists()) {
									log.info("directory created => : {}", dir.mkdirs());
								}
								String identifier = container.getPatientDemographics().getPatientIdentifier();
								String fileName = generateFileName(d, identifier);
								File file = new File(pathname + fileName);
								jaxbMarshaller.marshal(container, file);
								saveTheXmlFile(identifier, fileName,recaptureType);
								count.getAndIncrement();
								demographics.add(d);
							}
							
						} catch (JAXBException | SAXException e) {
							log.error("An error occurred while marshalling the container for patient with id {}  error {}",
									id, e.getMessage());
							count.decrementAndGet();
						}
					});
				});
		log.info("Total number of files generated {}", demographics.size());
		if (!demographics.isEmpty()) {
			zipAndSaveTheFilesforDownload(facilityId, pathname, count, demographics.get(0), "biometric_recapture_"+recaptureType);
		}
		
		return true;
	}
	
	
	private void saveTheXmlFile(String identifier, String fileName, Integer recaptureType) {
		NdrMessageLog ndrMessageLog = new NdrMessageLog();
		ndrMessageLog.setIdentifier(identifier);
		ndrMessageLog.setFile(fileName+"_"+recaptureType);
		ndrMessageLog.setLastUpdated(LocalDateTime.now());
		ndrMessageLog.setFileType("recaptured-biometric");
		ndrMessageLogRepository.save(ndrMessageLog);
	}



	public void zipAndSaveTheFilesforDownload(
			Long facilityId,
			String pathname,
			AtomicInteger count,
			PatientDemographicDTO patient, String type) {
		try {
			zipFiles(patient, facilityId, pathname, type);
		} catch (Exception e) {
			log.error("An error occurred while zipping files error {}", e.getMessage());
		}
	}


	public void zipFiles(PatientDemographicDTO demographic,
						 long facilityId,
						 String sourceFolder,
						  String type) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
		String sCode = demographic.getStateCode();
		String lCode = demographic.getLgaCode();
		String fileName = StringUtils.leftPad(sCode, 2, "0") +
				StringUtils.leftPad(lCode, 3, "0") + "_" + demographic.getFacilityId() +
				"_" + demographic.getFacilityName() +"_"+type+ "_" + dateFormat.format(new Date());
		fileName = RegExUtils.replaceAll(fileName, "/", "-");
		log.info("file name for download {}", fileName);
		String finalFileName = fileName.replace(" ", "").replace(",", "")
				.replace(".", "");
		String outputZipFile = null;
		try {
			outputZipFile = BASE_DIR + "ndr/" + finalFileName;
			new File(BASE_DIR + "ndr").mkdirs();
			new File(Paths.get(outputZipFile).toAbsolutePath().toString()).createNewFile();
			List<File> files = new ArrayList<>();
			files = getFiles(sourceFolder, files);
			long thirtyMB = (FileUtils.ONE_MB * 15)*2;
			File folder = new File(BASE_DIR + "temp/" + facilityId + "/");
			if (ZipUtility.getFolderSize(folder) > thirtyMB) {
				List<List<File>> splitFiles = split(files, thirtyMB);
				for (int i = 0; i < splitFiles.size(); i++) {
					String splitFileName = finalFileName + "_" + (i + 1);
					String splitOutputZipFile = BASE_DIR + "ndr/" + splitFileName;
					Path path = Paths.get(splitOutputZipFile);
					new File(path.toAbsolutePath().toString()).createNewFile();
					zip(splitFiles.get(i), path.toAbsolutePath().toString());
					storeTheFileInBD(facilityId, new AtomicInteger(splitFiles.get(i).size()), splitFileName,type);
				}
			} else {
				ZipUtility.zip(files, Paths.get(outputZipFile).toAbsolutePath().toString(), thirtyMB);
				storeTheFileInBD(facilityId, new AtomicInteger(files.size()), finalFileName,type);
			}
		} catch (Exception exception) {
			log.error("An error occurred while creating temporary file " + outputZipFile);
		}
	}


	public static List<List<File>> split(List<File> files, long sizeLimit) {
		List<List<File>> splitFiles = new ArrayList<>();
		List<File> currentSplit = new ArrayList<>();
		long currentSize = 0;
		for (File file : files) {
			long fileSize = file.length();
			if (currentSize + fileSize > sizeLimit) {
				splitFiles.add(currentSplit);
				currentSplit = new ArrayList<>();
				currentSize = 0;
			}
			currentSplit.add(file);
			currentSize += fileSize;
		}
		splitFiles.add(currentSplit);
		return splitFiles;
	}


	public void storeTheFileInBD(Long facilityId, AtomicInteger count,
								 String zipFileName, String type) {
		NdrXmlStatus ndrXmlStatus = new NdrXmlStatus();
		ndrXmlStatus.setFacilityId(facilityId);
		ndrXmlStatus.setFiles(count.get());
		ndrXmlStatus.setFileName(zipFileName);
		ndrXmlStatus.setLastModified(LocalDateTime.now());
		ndrXmlStatus.setPercentagePushed(0L);
		ndrXmlStatus.setType(type);
		ndrXmlStatusRepository.save(ndrXmlStatus);
	}
	
	

	private Marshaller getMarshaller(JAXBContext jaxbContext) throws JAXBException {
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		return marshaller;
	}
	
	
	private String generateFileName(PatientDemographicDTO demographics, String identifier) {
		return formulateFileName(demographics, identifier + "_bio_recapture", ndrCodeSetResolverService);
	}



	static String formulateFileName(PatientDemographicDTO demographics, String identifier, NDRCodeSetResolverService ndrCodeSetResolverService) {
		String sCode = "";
		String lCode = "";
		Optional<String> stateCode =
				ndrCodeSetResolverService.getNDRCodeSetCode("STATES", demographics.getState());
		if(stateCode.isPresent()) sCode = stateCode.get();
		Optional<String> lgaCode =
				ndrCodeSetResolverService.getNDRCodeSetCode("LGA", demographics.getLga());
		if(lgaCode.isPresent()) lCode = lgaCode.get();
		Date date = new Date ();
		SimpleDateFormat dateFormat = new SimpleDateFormat ("ddMMyyyy");
		String fileName = StringUtils.leftPad (sCode, 2, "0") +"_"+
				StringUtils.leftPad (lCode, 3, "0") +
				"_" + demographics.getFacilityId() + "_" + StringUtils.replace (identifier, "/", "-")
				+ "_" +dateFormat.format (date) + ".xml";
		return RegExUtils.replaceAll (fileName, "/", "-");
	}


	public void cleanupFacility(Long facilityId, String folder) {
		try {
			if (Files.isDirectory(Paths.get(folder))) {
				FileUtils.deleteDirectory(new File(folder));
			}
		} catch (IOException ignored) {
		}
	}


	public static void zip(List<File> files, String outputZipFile) throws IOException {
		try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputZipFile))) {
			for (File file : files) {
				try (FileInputStream fileIn = new FileInputStream(file)) {
					ZipEntry zipEntry = new ZipEntry(file.getName());
					zipOut.putNextEntry(zipEntry);
					byte[] bytes = new byte[1024];
					int length;
					while ((length = fileIn.read(bytes)) >= 0) {
						zipOut.write(bytes, 0, length);
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}



	public List<File> getFiles(String sourceFolder, List<File> files) {
		try (Stream<Path> walk = Files.walk (Paths.get (sourceFolder))) {
			files = walk.filter (Files::isRegularFile)
					.map (Path::toFile)
					.collect (Collectors.toList ());
		} catch (IOException e) {
			e.printStackTrace ();
		}
		return files;
	}

	@SneakyThrows
	public Iterable<NdrXmlStatusDto> getNdrStatus() {
		Iterable<NdrXmlStatus> ndrXmlStatusList = ndrXmlStatusRepository.getRecaptureFiles();
		List<NdrXmlStatusDto> ndrXmlStatusDtos = new ArrayList<>();
        for (NdrXmlStatus ndrXmlStatus : ndrXmlStatusList) {
            NdrXmlStatusDto ndrXmlStatusDto = new NdrXmlStatusDto();
            ndrXmlStatusDto.setFileName(ndrXmlStatus.getFileName());
            ndrXmlStatusDto.setFiles(ndrXmlStatus.getFiles());
            ndrXmlStatusDto.setLastModified(ndrXmlStatus.getLastModified());
            ndrXmlStatusDto.setId(ndrXmlStatus.getId());
            ndrXmlStatusDtos.add(ndrXmlStatusDto);
        }
		return ndrXmlStatusDtos;
	}

	@SneakyThrows
	public ByteArrayOutputStream downloadFile(String file) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		String folder = BASE_DIR + "ndr/";
		Optional<String> fileToDownload = listFilesUsingDirectoryStream (folder).stream ()
				.filter (f -> f.equals (file))
				.findFirst ();
		fileToDownload.ifPresent (s -> {
			try (InputStream is = new FileInputStream(folder + s)) {
				IOUtils.copy (is, baos);
			} catch (IOException ignored) {

			}
		});
		return baos;
	}

	public Set<String> listFilesUsingDirectoryStream(String dir) throws IOException {
		Set<String> fileList = new HashSet<> ();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream (Paths.get (dir))) {
			for (Path path : stream) {
				if (! Files.isDirectory (path)) {
					fileList.add (path.getFileName ().toString ());
				}
			}
		}
		return fileList;
	}

	@SneakyThrows
	public Set<String> listFiles() {
		String folder = BASE_DIR + "ndr";
		return listFilesUsingDirectoryStream (folder);
	}

}
