package com.melath.nubecula.storage.model.request;

import com.melath.nubecula.storage.model.reponse.ResponseFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestAction {

    List<ResponseFile> files;
    UUID targetDirId;
}
