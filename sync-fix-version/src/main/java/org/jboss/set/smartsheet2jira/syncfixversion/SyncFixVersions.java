/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2024, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.set.smartsheet2jira.syncfixversion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.SmartsheetFactory;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.PagedResult;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncFixVersions {
    public static void main(final String[] args) throws SmartsheetException, IOException {
        final String accessToken = args[0];
        System.out.println(new File(".").getAbsoluteFile());
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        // TODO: properly find default-transformers.yaml
        final List<Transformer> transformers = mapper.readValue(new File("sync-fix-version/src/main/resources/default-transformers.yaml"), new TypeReference<List<Transformer>>(){});
        final Smartsheet smartsheet = SmartsheetFactory.createDefaultClient(accessToken);
        final Map<String, Sheet> sheetsMap = new HashMap<>();
        final PagedResult<Sheet> sheets = smartsheet.sheetResources().listSheets();
        for (Sheet sheet : sheets.getData()) {
            sheetsMap.put(sheet.getName(), sheet);
        }
        for (Transformer transformer : transformers) {
            final String sheetName = transformer.getSheetName();
            final Sheet s = sheetsMap.get(sheetName);
            if (s == null) {
                System.err.println("Unable to find sheet " + sheetName);
                continue;
            }
            // now truly load the sheet
            final Sheet sheet = smartsheet.sheetResources().getSheet(s.getId());
            System.out.println(sheet.getName());
            final Map<String, Column> columnMap = new HashMap<>();
            for (Column column : sheet.getColumns()) {
                columnMap.put(column.getTitle(), column);
            }
            final Pattern pattern = Pattern.compile(transformer.getTaskPattern());
            final String format = transformer.getFixVersionFormat();
            final List<Row> rows = sheet.getRows();
            for (final Row row : rows) {
                final List<Cell> cells = row.getCells();
                final String taskName = Optional.ofNullable(cells.get(columnMap.get("Task Name").getIndex()).getValue()).orElse("").toString();
                final Matcher m = pattern.matcher(taskName);
                if (!m.matches()) continue;
                final String finish = Optional.ofNullable(cells.get(columnMap.get("Finish").getIndex()).getValue()).orElse("").toString();
                final String done = Optional.ofNullable(cells.get(columnMap.get("Done checkbox").getIndex()).getValue()).orElse("").toString();
                //System.out.println(taskName);
                final List<String> groups = new ArrayList<>();
                for (int i = 1; i <= m.groupCount(); i++)
                    groups.add(m.group(i));
                System.out.println(String.format(format, groups.toArray()) + " - " + finish + " - " + done);
            }
            //System.out.println(columnMap.get("Finish").getType()); // ABSTRACT_DATETIME
        }
    }
}
