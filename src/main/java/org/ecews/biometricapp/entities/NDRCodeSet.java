package org.ecews.biometricapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;


@Entity
@Table(name = "ndr_code_set")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NDRCodeSet implements Persistable<String> {
    @Id
   private String code;
   private String codeSetNm;
   private String codeDescription;
   private String altDescription;
   private String sysDescription;

   @Override
   public String getId() {
      return  code;
   }

   @Override
   public boolean isNew() {
      return code == null;
   }
}
