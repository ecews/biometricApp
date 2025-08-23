package org.ecews.biometricapp.services;

import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.entities.MPosition;
import org.ecews.biometricapp.repositories.MPositionRepository;
import org.springframework.stereotype.Service;

import javax.swing.text.Position;
import java.util.Optional;

@Service
@Slf4j
public class MPositionService {

    private final MPositionRepository mPositionRepository;

    public MPositionService(MPositionRepository mPositionRepository) {
        this.mPositionRepository = mPositionRepository;
    }

    public void saveUpdatePosition (MPosition position) {
        Optional<MPosition> mPosition = mPositionRepository.findById(position.getId());
        mPosition.ifPresentOrElse(
                value -> {
                    value.setMIndex(position.getMIndex());
                    mPositionRepository.save(value);
                },
                () -> mPositionRepository.save(position)
        );
    }
}
