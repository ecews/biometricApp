package org.ecews.biometricapp.controllers;


import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.dtos.NdrXmlStatusDto;
import org.ecews.biometricapp.entities.dtos.RecreateTemplateDTO;
import org.ecews.biometricapp.services.BiometricService;
import org.ecews.biometricapp.services.RecaptureBiometricService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/ndr/recapture")
@RequiredArgsConstructor
@Slf4j
public class BiometricRecaptureController {
	
	private  final RecaptureBiometricService biometricService;
	private final BiometricService service;
	
	@GetMapping("/facility/{facilityId}/{recaptureType}")
	@SneakyThrows
	public boolean generateRecaptureBiometricDetails(
			@PathVariable("facilityId") Long facilityId,
			@PathVariable("recaptureType") Integer recaptureType,
			@RequestParam("start") String start,
			@RequestParam("end") String end
			){
		Set<String> ids = new HashSet<>();
		log.info("Start **** {} ******* End {} ", start, end);
		if (!start.isEmpty() & !end.isEmpty()) {
			ids = service.getIdsForNDR(LocalDate.parse(start), LocalDate.parse(end));
		}
		List<Boolean> result = new ArrayList<>();
					boolean result1 = biometricService.generateRecaptureBiometrics(facilityId, recaptureType, ids);
					result.add(result1);
		return result.contains(true);
	}

	@PostMapping("/facility/{facilityId}/{recaptureType}/file")
	@SneakyThrows
	public boolean generateRecaptureBiometricDetailsFile(
			@PathVariable("facilityId") Long facilityId,
			@PathVariable("recaptureType") Integer recaptureType,
			@RequestParam("file") MultipartFile file
				){
		Set<String> ids = new HashSet<>();
		var dtos = service.readDTOsFromFile(file);
		ids =  dtos.stream().map(RecreateTemplateDTO::getPersonUuid).collect(Collectors.toSet());
		log.info("File **** {} ******* ", file);
		List<Boolean> result = new ArrayList<>();
		boolean result1 = biometricService.generateRecaptureBiometrics(facilityId, recaptureType, ids);
		result.add(result1);
		return result.contains(true);
	}


	@GetMapping("/download/{file}")
	public void downloadFile(@PathVariable String file, HttpServletResponse response) throws IOException {
		ByteArrayOutputStream baos = biometricService.downloadFile (file);
		response.setHeader ("Content-Type", "application/octet-stream");
		response.setHeader ("Content-Disposition", "attachment;filename=" + file + ".zip");
		response.setHeader ("Content-Length", Integer.toString (baos.size ()));
		OutputStream outputStream = response.getOutputStream ();
		outputStream.write (baos.toByteArray ());
		outputStream.close ();
		response.flushBuffer ();
	}

	@GetMapping("/files")
	public Iterable<NdrXmlStatusDto> listFiles() {
		return biometricService.getNdrStatus ();
	}
}
