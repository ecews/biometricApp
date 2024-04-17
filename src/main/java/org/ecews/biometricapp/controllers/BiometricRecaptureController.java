package org.ecews.biometricapp.controllers;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.dtos.NdrXmlStatusDto;
import org.ecews.biometricapp.services.RecaptureBiometricService;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/ndr/recapture")
@RequiredArgsConstructor
@Slf4j
public class BiometricRecaptureController {
	
	private  final RecaptureBiometricService biometricService;
	
	@GetMapping("/facility/{facilityId}/{recaptureType}")
	public boolean generateRecaptureBiometricDetails(@PathVariable("facilityId") Long facilityId, @PathVariable("recaptureType") Integer recaptureType){
		List<Boolean> result = new ArrayList<>();
					boolean result1 = biometricService.generateRecaptureBiometrics(facilityId, recaptureType);
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
