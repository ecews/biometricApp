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
import org.ecews.biometricapp.entities.dtos.NdrXmlStatusDto;
import org.ecews.biometricapp.entities.dtos.PatientDemographics;
import org.ecews.biometricapp.recapture.Container;
import org.ecews.biometricapp.repositories.NDRCodeSetRepository;
import org.ecews.biometricapp.repositories.NdrMessageLogRepository;
import org.ecews.biometricapp.repositories.NdrXmlStatusRepository;
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
		List<String> patientsIds = nDRCodeSetRepository.getRecapturedPatientIds(facilityId, recaptureType);
		log.info("About {} patients are identified for generating NDR file", patientsIds.size());
		if (patientsIds.isEmpty()) {
			return false;
		}
		log.info("fetching patient demographics");
		List<PatientDemographics> demographics = new ArrayList<>();
		patientsIds.parallelStream()
				.forEach(id -> {
					Optional<PatientDemographics> demographicsOptional =
							ndrXmlStatusRepository.getPatientDemographicsByUUID(id);
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
								saveTheXmlFile(identifier, fileName);
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
			zipAndSaveTheFilesforDownload(facilityId, pathname, count, demographics);
		}
		
		return true;
	}
	
	
	private void saveTheXmlFile(String identifier, String fileName) {
		NdrMessageLog ndrMessageLog = new NdrMessageLog();
		ndrMessageLog.setIdentifier(identifier);
		ndrMessageLog.setFile(fileName);
		ndrMessageLog.setLastUpdated(LocalDateTime.now());
		ndrMessageLog.setFileType("recaptured-biometric");
		ndrMessageLogRepository.save(ndrMessageLog);
	}
	
	
	private void zipAndSaveTheFilesforDownload(
			Long facilityId,
			String pathname,
			AtomicInteger count,
			List<PatientDemographics> demographics) {
		try {
		String zipFileName = zipFileWithType(demographics.get(0), pathname, "bio_recapture");
		NdrXmlStatus ndrXmlStatus = new NdrXmlStatus();
		ndrXmlStatus.setFacilityId(facilityId);
		ndrXmlStatus.setFiles(count.get());
		ndrXmlStatus.setFileName(zipFileName);
		ndrXmlStatus.setLastModified(LocalDateTime.now());
		ndrXmlStatus.setPushIdentifier(demographics.get(0).getDatimId().concat("_").concat(demographics.get(0).getPersonUuid()));
		ndrXmlStatus.setPercentagePushed(0L);
		ndrXmlStatus.setType("recaptured-biometric");
		ndrXmlStatusRepository.save(ndrXmlStatus);
		}catch (Exception e){
			log.error("An error occurred while zipping files error {}", e.getMessage());
		}
	}
	
	private Marshaller getMarshaller(JAXBContext jaxbContext) throws JAXBException {
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		return marshaller;
	}
	
	
	private String generateFileName(PatientDemographics demographics, String identifier) {
		return formulateFileName(demographics, identifier + "_bio_recapture", ndrCodeSetResolverService);
	}



	static String formulateFileName(PatientDemographics demographics, String identifier, NDRCodeSetResolverService ndrCodeSetResolverService) {
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
				"_" + demographics.getDatimId() + "_" + StringUtils.replace (identifier, "/", "-")
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


	public String zipFileWithType(PatientDemographics demographics, String sourceFolder, String type) {
		SimpleDateFormat dateFormat = new SimpleDateFormat ("ddMMyyyy");
		String sCode = "";
		String lCode = "";
		Optional<String> stateCode =
				ndrCodeSetResolverService.getNDRCodeSetCode("STATES", demographics.getState());
		if(stateCode.isPresent()) sCode = stateCode.get();
		Optional<String> lgaCode =
				ndrCodeSetResolverService.getNDRCodeSetCode("LGA", demographics.getLga());
		if(lgaCode.isPresent()) lCode = lgaCode.get();
		String fileName = StringUtils.leftPad (sCode, 2, "0") +
				StringUtils.leftPad ( lCode, 3, "0") + "_" + demographics.getDatimId() +
				"_" + demographics.getFacilityName()+"_"+type+ "_" + dateFormat.format (new Date());

		fileName = RegExUtils.replaceAll (fileName, "/", "-");
		log.info ("file name for download {}", fileName);
		String finalFileName = fileName.replace(" ", "").replace(",", "")
				.replace(".", "");
		String outputZipFile = null;
		try {
			outputZipFile = BASE_DIR + "ndr/" + finalFileName;
			new File (BASE_DIR + "ndr").mkdirs ();
			new File (Paths.get (outputZipFile).toAbsolutePath ().toString ()).createNewFile ();
			List<File> files = new ArrayList<> ();
			files = getFiles (sourceFolder, files);
			log.info ("Files: {}", files);
			long fifteenMB = FileUtils.ONE_MB * 15;
			ZipUtility.zip (files, Paths.get (outputZipFile).toAbsolutePath ().toString (), fifteenMB);
			return finalFileName;
		} catch (Exception exception) {
			log.error ("An error occurred while creating temporary file " + outputZipFile);
		}
		return null;
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
		Iterator iterator = ndrXmlStatusList.iterator();
		while (iterator.hasNext()){
			NdrXmlStatus ndrXmlStatus = (NdrXmlStatus) iterator.next();
			NdrXmlStatusDto ndrXmlStatusDto = new NdrXmlStatusDto();
			//ndrXmlStatusDto.setFacility((organisationUnitService.getOrganizationUnit (ndrXmlStatus.getFacilityId ()).getName ()));
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
