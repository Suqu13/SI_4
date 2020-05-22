import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern
import kotlin.streams.toList


class DataLoader {

    fun load(fileName: String, pathToDirectoryWithData: String) {
        val labels = getLabels(pathToDirectoryWithData)
        val content = mutableListOf<String>()
        content.add("@relation dataset\n")
        content.add("@attribute content_attr string\n")
        content.add("@attribute label_attr {${labels.joinToString(", ")}}\n")
        content.add("@data\n")
        content.add(getData(pathToDirectoryWithData))

        val file = File(fileName)
        file.writeText(content.joinToString(""))
    }

    private fun getLabels(path: String): Set<String> {
        val fileSNames = Files.walk(Paths.get(path)).use { walk ->
            return@use walk.map { x: Path -> x.fileName.toString() }
                .filter { f -> f.endsWith(".txt") }.toList()
        }
        return fileSNames.map { extractCategoryNameFromFileName(it) }.sorted().toSet()
    }

    private fun extractCategoryNameFromFileName(fileName: String): String = fileName.split("_")[0]

    private fun getData(path: String): String {
        val data = mutableListOf<String>()
        Files.walk(Paths.get(path)).use { walk ->
            return@use walk.map { x: Path -> x }
                .filter { f -> f.toString().endsWith(".txt") }
                .forEach {
                    it.toFile().useLines { lines ->
                        val cleanedText = cleanUpText(lines.joinToString(" "))
                        data.add("'$cleanedText', ${extractCategoryNameFromFileName(it.fileName.toString())}")
                    }
                }
        }
        return data.joinToString("\n")
    }

    private fun cleanUpText(text: String): String {
        val regex = Pattern.compile("[^\\w\\s]|[\\d]|[\n]", Pattern.UNICODE_CHARACTER_CLASS).toRegex()
        return text.replace(regex, " ")
            .replace("\\s+".toRegex(), " ")
            .toLowerCase().trim()
    }
}

fun main() {
    val dataLoader = DataLoader()
    dataLoader.load("data.arff", "src/main/resources/wiki_test_34_categories_data")
}
