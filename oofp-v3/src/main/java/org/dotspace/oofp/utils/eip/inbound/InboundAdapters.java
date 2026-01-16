package org.dotspace.oofp.utils.eip.inbound;

import org.dotspace.oofp.model.dto.eip.InboundEnvelope;
import org.dotspace.oofp.model.dto.eip.MqttMeta;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.time.Instant;
import java.util.Map;

@UtilityClass
public class InboundAdapters {

    public <T, R> InboundEnvelope<R> fromSource(
            InboundEnvelope.InboundSource source,
            String sourceId,
            Map<String, String> meta,
            InboundSourceReader<T, R> reader,
            T raw,
            Instant receivedAt
    ) {
        return new InboundEnvelope<>(
                source,
                sourceId,
                null == meta ? Map.of() : Map.copyOf(meta),
                null != raw ? reader.read(raw) : null,
                receivedAt == null ? Instant.now() : receivedAt
        );
    }

    public <T, R> InboundEnvelope<R> fromSource(
            InboundEnvelope.InboundSource source,
            String sourceId,
            Map<String, String> meta,
            InboundSourceReader<T, R> reader, T raw) {
        return fromSource(source, sourceId, meta, reader, raw, Instant.now());
    }

    public <T> InboundEnvelope<T> fromHttp(
            InboundMetaExtractor<HttpServletRequest> metaExtractor, @NonNull HttpServletRequest req, T body) {
        return fromSource(
                InboundEnvelope.InboundSource.HTTP,
                req.getRequestURI(),
                metaExtractor.extract(req),
                InboundSourceReader.identity(), body
        );
    }

    public <T> InboundEnvelope<T> fromHttp(
            @NonNull String requestUri, @NonNull Map<String, String> meta, T body) {
        return fromSource(
                InboundEnvelope.InboundSource.HTTP,
                requestUri,
                meta,
                InboundSourceReader.identity(), body
        );
    }

    public <T> InboundEnvelope<T> fromMqtt(
            String topic,
            Map<String, String> rawMeta,
            InboundSourceReader<byte[], T> parser,
            byte[] bytes) {
        return fromSource(
                InboundEnvelope.InboundSource.MQTT,
                topic,
                MqttMeta.normalize(rawMeta),
                parser,
                bytes
        );
    }

    public <T> InboundEnvelope<T> fromMq(
            String topic, Map<String,String> headers,
            InboundSourceReader<byte[], T> parser, byte[] bytes) {
        return fromSource(
                InboundEnvelope.InboundSource.MQ,
                topic,
                headers,
                parser,
                bytes
        );
    }

    public <T> InboundEnvelope<T> fromFile(Map<String, String> meta, InboundSourceReader<File, T> reader, File file) {
        return fromSource(
                InboundEnvelope.InboundSource.FILE,
                file.getAbsolutePath(),
                meta,
                reader,
                file
        );
    }

}
