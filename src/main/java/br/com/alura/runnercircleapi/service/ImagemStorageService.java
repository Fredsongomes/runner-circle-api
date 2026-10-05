package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.exception.ImagemInvalidaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ImagemStorageService {

    private static final long TAMANHO_MAXIMO_BYTES = 5 * 1024 * 1024;
    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> CONTENT_TYPES_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path diretorioUploads;

    public ImagemStorageService(@Value("${app.upload.dir}") String diretorioUploads) {
        this.diretorioUploads = Paths.get(diretorioUploads).toAbsolutePath().normalize();
    }

    public String salvar(MultipartFile imagem) {
        String extensao = validar(imagem);
        String nomeArquivo = UUID.randomUUID() + "." + extensao;

        try (InputStream conteudo = imagem.getInputStream()) {
            Files.createDirectories(diretorioUploads);
            Files.copy(conteudo, diretorioUploads.resolve(nomeArquivo));
        } catch (IOException e) {
            throw new UncheckedIOException("não foi possível salvar a imagem", e);
        }

        return "/uploads/" + nomeArquivo;
    }

    private String validar(MultipartFile imagem) {
        if (imagem.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new ImagemInvalidaException("a imagem deve ter no máximo 5 MB");
        }

        String extensao = extrairExtensao(imagem.getOriginalFilename());
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new ImagemInvalidaException("extensão inválida: use jpg, jpeg, png ou webp");
        }

        String contentType = imagem.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ImagemInvalidaException("tipo de arquivo inválido: envie uma imagem jpg, png ou webp");
        }

        return extensao;
    }

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) {
            return "";
        }
        return nomeOriginal.substring(nomeOriginal.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
