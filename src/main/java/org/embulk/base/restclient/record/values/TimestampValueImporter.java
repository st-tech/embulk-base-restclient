/*
 * Copyright 2016 The Embulk project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.embulk.base.restclient.record.values;

import java.time.Instant;
import org.embulk.base.restclient.record.ServiceRecord;
import org.embulk.base.restclient.record.ServiceValue;
import org.embulk.base.restclient.record.ValueImporter;
import org.embulk.base.restclient.record.ValueLocator;
import org.embulk.spi.Column;
import org.embulk.spi.DataException;
import org.embulk.spi.PageBuilder;
import org.embulk.util.timestamp.TimestampFormatter;

public class TimestampValueImporter extends ValueImporter {
    public TimestampValueImporter(
            final Column column, final ValueLocator valueLocator, final TimestampFormatter timestampFormatter) {
        super(column, valueLocator);
        this.timestampFormatter = timestampFormatter;
    }

    @Override
    public void findAndImportValue(final ServiceRecord record, final PageBuilder pageBuilder) {
        try {
            final ServiceValue value = findValue(record);
            if (value == null || value.isNull()) {
                pageBuilder.setNull(getColumnToImport());
            } else {
                setTimestampToPageBuilder(pageBuilder, getColumnToImport(), value.timestampValue(timestampFormatter));
            }
        } catch (final Exception ex) {
            throw new DataException(
                    "Failed to import a value for column: " + getColumnToImport().getName()
                    + " (" + getColumnToImport().getType().getName() + ")",
                    ex);
        }
    }

    @SuppressWarnings("deprecation")  // org.embulk.spi.time.Timestamp
    private static void setTimestampToPageBuilder(final PageBuilder pageBuilder, final Column column, final Instant instant) {
        pageBuilder.setTimestamp(column, org.embulk.spi.time.Timestamp.ofInstant(instant));
    }

    private final TimestampFormatter timestampFormatter;
}
