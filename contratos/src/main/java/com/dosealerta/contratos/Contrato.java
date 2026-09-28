package com.dosealerta.contratos;

import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public final class Contrato {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final String nome;
	private final Schema schema;
	private final JsonNode definicao;

	private Contrato(String nome, JsonNode definicao, Schema schema) {
		this.nome = nome;
		this.definicao = definicao;
		this.schema = schema;
	}

	public static Contrato carregar(String nome) {
		String recurso = "/contratos/" + nome + ".schema.json";
		try (InputStream in = Contrato.class.getResourceAsStream(recurso)) {
			if (in == null) {
				throw new IllegalArgumentException("Contrato inexistente: " + recurso);
			}
			JsonNode definicao = MAPPER.readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8));
			Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12, registro -> registro.schemaRegistryConfig(
							SchemaRegistryConfig.builder().formatAssertionsEnabled(true).build()))
					.getSchema(definicao.toString(), InputFormat.JSON);
			return new Contrato(nome, definicao, schema);
		} catch (IOException e) {
			throw new IllegalStateException("Falha ao ler o contrato " + nome, e);
		}
	}

	public String exemplo() {
		return definicao.get("examples").get(0).toString();
	}

	public JsonNode exemploComoArvore() {
		return definicao.get("examples").get(0);
	}

	public List<String> camposObrigatorios() {
		return MAPPER.convertValue(definicao.get("required"), MAPPER.getTypeFactory().constructCollectionType(List.class, String.class));
	}

	public Set<String> valoresPermitidos(String campo) {
		Set<String> valores = new TreeSet<>();
		definicao.get("properties").get(campo).get("enum").forEach(v -> valores.add(v.asString()));
		return valores;
	}

	public Set<String> violacoes(String json) {
		return schema.validate(json, InputFormat.JSON).stream()
				.map(erro -> erro.getMessage())
				.collect(Collectors.toSet());
	}

	public String semCampo(String campo) {
		var copia = exemploComoArvore().deepCopy();
		((tools.jackson.databind.node.ObjectNode) copia).remove(campo);
		return copia.toString();
	}

	@Override
	public String toString() {
		return "Contrato[" + nome + "]";
	}
}
