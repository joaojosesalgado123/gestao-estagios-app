package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

const val ATIVIDADE_CATEGORIA_DESENVOLVIMENTO = "Desenvolvimento"
const val ATIVIDADE_CATEGORIA_REUNIAO = "Reunião"
const val ATIVIDADE_CATEGORIA_IMPORTANTE = "Importante"
private const val ATIVIDADE_CATEGORIA_IMPORTANTE_LEGACY = "Coisas importantes que pertencem ao estágio"

val ATIVIDADE_CATEGORIAS = listOf(
    ATIVIDADE_CATEGORIA_DESENVOLVIMENTO,
    ATIVIDADE_CATEGORIA_REUNIAO,
    ATIVIDADE_CATEGORIA_IMPORTANTE
)

private const val CATEGORIA_PREFIXO = "[categoria:"
private const val CATEGORIA_FECHO = "]"

data class Atividade(
    @SerializedName("idatividade") val idAtividade: String = "",
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("data_atividade") val dataAtividade: String? = null,
    @SerializedName("data_registo") val dataRegisto: String = "",
    @SerializedName("idestagio") val idEstagio: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)

fun normalizarCategoriaAtividade(categoria: String): String = when (categoria) {
    ATIVIDADE_CATEGORIA_DESENVOLVIMENTO -> ATIVIDADE_CATEGORIA_DESENVOLVIMENTO
    ATIVIDADE_CATEGORIA_REUNIAO -> ATIVIDADE_CATEGORIA_REUNIAO
    ATIVIDADE_CATEGORIA_IMPORTANTE, ATIVIDADE_CATEGORIA_IMPORTANTE_LEGACY -> ATIVIDADE_CATEGORIA_IMPORTANTE
    else -> ATIVIDADE_CATEGORIA_DESENVOLVIMENTO
}

fun descricaoComCategoriaAtividade(categoria: String, descricao: String): String =
    "$CATEGORIA_PREFIXO${normalizarCategoriaAtividade(categoria)}$CATEGORIA_FECHO\n${descricao.trim()}"

fun extrairCategoriaAtividade(descricao: String?): String {
    val texto = descricao.orEmpty()
    if (!texto.startsWith(CATEGORIA_PREFIXO)) return ATIVIDADE_CATEGORIA_DESENVOLVIMENTO

    val fim = texto.indexOf(CATEGORIA_FECHO)
    if (fim == -1) return ATIVIDADE_CATEGORIA_DESENVOLVIMENTO

    return normalizarCategoriaAtividade(texto.substring(CATEGORIA_PREFIXO.length, fim))
}

fun descricaoSemCategoriaAtividade(descricao: String?): String {
    val texto = descricao.orEmpty()
    if (!texto.startsWith(CATEGORIA_PREFIXO)) return texto

    val fim = texto.indexOf(CATEGORIA_FECHO)
    if (fim == -1) return texto

    return texto.substring(fim + CATEGORIA_FECHO.length).trimStart()
}

fun Atividade.categoriaAtividade(): String = extrairCategoriaAtividade(descricao)

fun Atividade.descricaoVisivel(): String = descricaoSemCategoriaAtividade(descricao)
