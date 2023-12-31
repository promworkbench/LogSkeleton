package org.processmining.logskeleton.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.models.LogSkeleton;

import com.csvreader.CsvWriter;

@Plugin( //
		name = "Export Log Skeleton", //
		returnLabels = {}, //
		returnTypes = {}, //
		parameterLabels = { "Log Skeleton", "File" }, //
		userAccessible = true //
) //
@UIExportPlugin( //
		description = "Log Skeleton", //
		extension = "lsk" //
) //
public class ExportPlugin {

	@PluginVariant(variantLabel = "Export Log Skeleton", requiredParameterLabels = { 0, 1 })
	public void export(PluginContext context, LogSkeleton logSkeleton, File file) throws IOException {
		Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
		CsvWriter csvWriter = new CsvWriter(fileWriter, ',');
		logSkeleton.exportToFile(csvWriter);
		csvWriter.close();
	}
}
