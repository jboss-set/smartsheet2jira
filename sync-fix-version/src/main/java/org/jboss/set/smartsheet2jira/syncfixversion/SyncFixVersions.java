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

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.SmartsheetFactory;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncFixVersions {
    private static final Map<String, Column> columnMap = new HashMap<>();
    private static final Pattern pattern = Pattern.compile("CP GA EAP 8\\.0\\.(\\d[\\.\\d]*).*");
    private static final String format = "8.0 Update %s";

    public static void main(final String[] args) throws SmartsheetException {
        final String accessToken = args[0];
        final Smartsheet smartsheet = SmartsheetFactory.createDefaultClient(accessToken);
        /*
        final PagedResult<Sheet> sheets = smartsheet.sheetResources().listSheets();
        for (Sheet sheet : sheets.getData()) {
            System.out.println(sheet.getName());
            System.out.println("> " + sheet.getId());
        }
        */
        final Sheet sheet = smartsheet.sheetResources().getSheet(8602266509830020L); // should be EAP 8.0.z. v10 _Latest version
        System.out.println(sheet.getName());
        for (Column column : sheet.getColumns()) {
            columnMap.put(column.getTitle(), column);
        }
        //System.out.println(columnMap.get("Finish").getType()); // ABSTRACT_DATETIME
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
    }
}
