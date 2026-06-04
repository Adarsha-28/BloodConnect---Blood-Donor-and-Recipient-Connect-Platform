package com.blooddonation.service;

import com.blooddonation.enums.BloodGroup;
import com.blooddonation.exception.ResourceNotFoundException;
import com.blooddonation.model.BloodBank;
import com.blooddonation.model.BloodInventory;
import com.blooddonation.repository.BloodBankRepository;
import com.blooddonation.repository.BloodInventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class BloodBankService {

    private final BloodBankRepository bloodBankRepository;
    private final BloodInventoryRepository bloodInventoryRepository;

    public BloodBankService(BloodBankRepository bloodBankRepository,
                            BloodInventoryRepository bloodInventoryRepository) {
        this.bloodBankRepository = bloodBankRepository;
        this.bloodInventoryRepository = bloodInventoryRepository;
    }

    public List<BloodBank> getAllBanks(String city) {
        log.info("Fetching all blood banks (city filter: {})", city);
        if (city != null && !city.trim().isEmpty()) {
            return bloodBankRepository.findByCityIgnoreCase(city);
        }
        return bloodBankRepository.findAll();
    }

    public BloodBank getBankById(Long id) {
        log.info("Fetching blood bank by ID: {}", id);
        return bloodBankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found with id: " + id));
    }

    public List<BloodInventory> getInventoryByBank(Long bankId) {
        log.info("Fetching inventory for blood bank ID: {}", bankId);
        // Verify bank exists
        getBankById(bankId);
        return bloodInventoryRepository.findByBloodBankId(bankId);
    }

    @Transactional
    public BloodInventory updateInventory(Long bankId, BloodGroup bg, int units) {
        log.info("Updating inventory for bank ID: {} - Blood Group: {} to {} units", bankId, bg, units);
        BloodBank bank = getBankById(bankId);

        BloodInventory inventory = bloodInventoryRepository.findByBloodBankIdAndBloodGroup(bankId, bg)
                .orElseGet(() -> BloodInventory.builder()
                        .bloodBank(bank)
                        .bloodGroup(bg)
                        .unitsAvailable(0)
                        .build());

        inventory.setUnitsAvailable(units);
        BloodInventory savedInventory = bloodInventoryRepository.save(inventory);
        log.info("Inventory updated successfully for bank ID: {}", bankId);
        return savedInventory;
    }
}
