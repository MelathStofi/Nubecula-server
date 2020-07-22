package com.melath.nubecula.storage.repository.custom;

import com.melath.nubecula.storage.model.entity.NubeculaFile;

public interface FileRepositoryCustom {

    void detach(NubeculaFile nubeculaFile);
}
