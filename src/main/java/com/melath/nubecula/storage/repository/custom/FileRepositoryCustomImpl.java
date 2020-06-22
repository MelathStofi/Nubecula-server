package com.melath.nubecula.storage.repository.custom;

import com.melath.nubecula.storage.model.NubeculaFile;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class FileRepositoryCustomImpl implements FileRepositoryCustom{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void detach(NubeculaFile nubeculaFile) {
        entityManager.detach(nubeculaFile);
    }
}
