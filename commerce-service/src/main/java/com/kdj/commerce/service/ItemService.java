package com.kdj.commerce.service;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.web.dto.item.ItemForm;
import com.kdj.commerce.web.file.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    public Item findOne(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + id));
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Transactional
    public Long save(ItemForm form, Long memberId) {
        MultipartFile imageFile = form.getImageFile();

        String storeFileName;
        try {
            storeFileName = fileStore.storeFile(imageFile);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장에 실패했습니다.", e);
        }

        String uploadFileName =
                (imageFile == null || imageFile.isEmpty())
                        ? null
                        : imageFile.getOriginalFilename();

        Item item = Item.createItem(
                form.getName(),
                form.getPrice(),
                form.getStock(),
                form.getDescription(),
                form.isOpen(),
                form.isDeleted(),
                form.getItemType(),
                form.getDeliveryType(),
                memberId,
                uploadFileName,
                storeFileName
        );

        return itemRepository.save(item).getId();
    }

    @Transactional
    public void update(Long id, ItemForm form) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + id));

        String storeFileName = item.getStoreFileName();
        String uploadFileName = item.getUploadFileName();

        MultipartFile imageFile = form.getImageFile();

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                storeFileName = fileStore.storeFile(imageFile);
            } catch (IOException e) {
                throw new IllegalStateException("파일 저장에 실패했습니다.", e);
            }

            uploadFileName = imageFile.getOriginalFilename();
        }

        item.update(
                form.getName(),
                form.getPrice(),
                form.getStock(),
                form.getDescription(),
                form.isOpen(),
                form.getItemType(),
                form.getDeliveryType(),
                uploadFileName,
                storeFileName
        );
    }

    @Transactional
    public void delete(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + id));
        item.delete();
    }

    @Transactional
    public void restore(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + id));
        item.restore();
    }

    public Page<Item> findByCreatedBy(Pageable pageable, Long id) {
        return itemRepository.findByCreatedBy(pageable, id);
    }

    public Page<Item> findActive(Pageable pageable) {
        return itemRepository.findByDeletedFalse(pageable);
    }
}
