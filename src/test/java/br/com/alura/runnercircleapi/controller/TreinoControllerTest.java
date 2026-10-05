package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.LoginRequestDTO;
import br.com.alura.runnercircleapi.dto.RegisterRequestDTO;
import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.model.Role;
import br.com.alura.runnercircleapi.model.TipoTreino;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TreinoControllerTest {

    private static final String SENHA = "senha12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void criarTreino_comDadosValidos_retorna201() throws Exception {
        String email = emailUnico();
        String token = registrarELogar(email);
        TreinoRequestDTO dto = new TreinoRequestDTO(TipoTreino.CORRIDA, 30, 5000, 300, 140, "corrida leve");

        mockMvc.perform(multipart("/treinos")
                        .file(parteDados(objectMapper.writeValueAsBytes(dto)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                // o autor vem do token, não de um parâmetro da requisição
                .andExpect(jsonPath("$.autor.username").value(email.substring(0, email.indexOf('@'))));
    }

    @Test
    void criarTreino_semToken_retorna401() throws Exception {
        TreinoRequestDTO dto = new TreinoRequestDTO(TipoTreino.CORRIDA, 30, 5000, 300, 140, "corrida sem token");

        mockMvc.perform(multipart("/treinos")
                        .file(parteDados(objectMapper.writeValueAsBytes(dto))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value("autenticação necessária"))
                .andExpect(jsonPath("$.caminho").value("/treinos"));
    }

    @Test
    void criarTreino_semTipoTreino_retorna400() throws Exception {
        String token = registrarELogar(emailUnico());

        Map<String, Object> corpoSemTipo = new LinkedHashMap<>();
        corpoSemTipo.put("tempoEmMinutos", 30);
        corpoSemTipo.put("distanciaMetros", 5000);
        corpoSemTipo.put("calorias", 300);
        corpoSemTipo.put("batimentos", 140);
        corpoSemTipo.put("descricao", "corrida sem tipo");

        mockMvc.perform(multipart("/treinos")
                        .file(parteDados(objectMapper.writeValueAsBytes(corpoSemTipo)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("tipoTreino")));
    }

    @Test
    void buscarTreino_quandoNaoExiste_retorna404() throws Exception {
        String token = registrarELogar(emailUnico());

        mockMvc.perform(get("/treinos/9999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("treino 9999 não encontrado"));
    }

    @Test
    void listarUsuarios_comTokenDeUsuarioComum_retorna403() throws Exception {
        String token = registrarELogar(emailUnico());

        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem").value("acesso negado"));
    }

    @Test
    void listarUsuarios_comTokenDeAdmin_retorna200() throws Exception {
        String email = emailUnico();
        registrar(email);

        // Não existe endpoint para promover a ADMIN: altera direto no banco.
        // O login vem depois porque a role é gravada no token no momento do login.
        User pessoa = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        pessoa.setRole(Role.ADMIN);
        userRepository.save(pessoa);

        String token = login(email);

        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.email == '" + email + "')].role").value("ADMIN"));
    }

    // Registra uma pessoa nova via POST /auth/register, faz login via POST /auth/login e devolve o token JWT.
    private String registrarELogar(String email) throws Exception {
        registrar(email);
        return login(email);
    }

    private void registrar(String email) throws Exception {
        String username = email.substring(0, email.indexOf('@'));
        RegisterRequestDTO dto = new RegisterRequestDTO("Pessoa Teste", username, email, SENHA);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isCreated());
    }

    private String login(String email) throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO(email, SENHA, false);

        String resposta = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(resposta).get("token").asString();
    }

    // Username e email únicos por teste, porque o H2 é compartilhado entre os testes da mesma execução.
    private static String emailUnico() {
        return "pessoa_" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
    }

    // POST /treinos é multipart: o JSON do treino vai na parte "dados", com Content-Type application/json.
    private static MockMultipartFile parteDados(byte[] json) {
        return new MockMultipartFile("dados", "", MediaType.APPLICATION_JSON_VALUE, json);
    }
}
