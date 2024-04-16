package org.ecews.biometricapp.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NDREligibleClient {
	String  personUuid;
	String  hospitalNumber;
	String  Name;
}
