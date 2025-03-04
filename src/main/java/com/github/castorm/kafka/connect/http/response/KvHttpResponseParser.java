package com.github.castorm.kafka.connect.http.response;

/*-
 * #%L
 * kafka-connect-http
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.record.spi.KvSourceRecordMapper;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.github.castorm.kafka.connect.http.response.spi.KvRecordHttpResponseParser;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.connect.source.SourceRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j
public class KvHttpResponseParser implements HttpResponseParser {

    private final Function<Map<String, ?>, KvHttpResponseParserConfig> configFactory;

    private KvRecordHttpResponseParser recordParser;

    private KvSourceRecordMapper recordMapper;

    private boolean policy;

    public KvHttpResponseParser() {
        this(KvHttpResponseParserConfig::new);
    }

    @Override
    public void configure(Map<String, ?> configs) {
        KvHttpResponseParserConfig config = configFactory.apply(configs);
        recordParser = config.getRecordParser();
        recordMapper = config.getRecordMapper();
        policy = config.isSkipError();
    }

    @Override
    public List<SourceRecord> parse(HttpResponse response) {

        if (policy) {
            try {
                return parseResponse(response);
            } catch (Exception ex) {
                log.warn("Error parsing http response (Skipped and continue polling)", ex);
                return emptyList();
            }
        } else {
            return parseResponse(response);
        }
    }

    private List<SourceRecord> parseResponse(HttpResponse response) {
        return recordParser.parse(response).stream()
                .map(recordMapper::map)
                .collect(toList());
    }
}
