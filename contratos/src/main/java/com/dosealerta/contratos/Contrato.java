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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Acesso a um contrato de payload entre modulos: o JSON Schema em {@code contratos/<nome>.schema.json}
 * e o exemplo canonico declarado nele ({@code examples[0]}).
 */
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

	/** Exemplo canonico do contrato, como JSON. */
	public String exemplo() {
		return definicao.get("examples").get(0).toString();
	}

	public JsonNode exemploComoArvore() {
		return definicao.get("examples").get(0);
	}

	public List<String> camposObrigatorios() {
		return MAPPER.convertValue(definicao.get("required"), MAPPER.getTypeFactory().constructCollectionType(List.class, String.class));
	}

	/** Valores que o schema permite para um campo enumerado. */
	public java.util.Set<String> valoresPermitidos(String campo) {
		java.util.Set<String> valores = new java.util.TreeSet<>();
		definicao.get("properties").get(campo).get("enum").forEach(v -> valores.add(v.asString()));
		return valores;
	}

	/** Mensagens de violacao do payload contra o schema; vazio se o payload respeita o contrato. */
	public Set<String> violacoes(String json) {
		return schema.validate(json, InputFormat.JSON).stream()
				.map(erro -> erro.getMessage())
				.collect(java.util.stream.Collectors.toSet());
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
