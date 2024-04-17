package org.ecews.biometricapp.entities.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NDRErrorDTO {
	private  String patientUuid ;
	private  String hospitalNumber ;
	private  String  errorMessage ;
}
