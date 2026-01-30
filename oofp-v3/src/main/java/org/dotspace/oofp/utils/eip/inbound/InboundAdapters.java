package org.dotspace.oofp.utils.eip.inbound;

import org.dotspace.oofp.model.dto.eip.InboundEnvelope;
import org.dotspace.oofp.model.dto.eip.InboundMetaHeaders;
import org.dotspace.oofp.model.dto.eip.InboundMetaKeys;
import org.dotspace.oofp.model.dto.eip.MqttMeta;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class InboundAdapters {

    public <T, R> InboundEnvelope<R> fromSource(
            InboundEnvelope.InboundSource source,
            String sourceId,
            Map<String, String> meta,
            InboundSourceReader<T, R> reader,
            T raw,
            Instant receivedAt) {
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

    public <T> InboundEnvelope<T> fromHttp(T body) {
        return Maybe.given(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(req -> fromHttp(req, body))
                .orElseGet(() -> fromSource(
                        InboundEnvelope.InboundSource.EMPTY_HTTP_CONTEXT,
                        "no-http-request",
                        Map.of("reason", "RequestContextHolder has no ServletRequestAttributes"),
                        InboundSourceReader.identity(),
                        body,
                        Instant.now()
                ));
    }

    public <T> InboundEnvelope<T> fromHttp(@NonNull HttpServletRequest req, T body) {
        Pair<String, Map<String, String>> sourceIdAndMeta = getHttpSourceIdWithMeta(req);

        return fromSource(
                InboundEnvelope.InboundSource.HTTP,
                sourceIdAndMeta.getLeft(),
                Map.copyOf(sourceIdAndMeta.getRight()),
                InboundSourceReader.identity(), body
        );
    }

    private Pair<String, Map<String, String>> getHttpSourceIdWithMeta(@NonNull HttpServletRequest req) {
        String method = req.getMethod();
        String uri = req.getRequestURI();
        String query = req.getQueryString();

        // meta：保留「足夠決策 + 足夠追蹤」但不要無上限膨脹
        Map<String, String> meta = new java.util.HashMap<>();

        // --- trace id ---
        String traceId = req.getHeader(InboundMetaHeaders.TRACE_ID);

        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        meta.put(InboundMetaKeys.TRACE_ID, traceId);

        meta.put("http.method", method);
        meta.put("http.uri", uri);
        if (query != null && !query.isBlank()) {
            meta.put("http.query", query);
        }

        String auth = req.getHeader("Authorization");
        if (auth != null && !auth.isBlank()) {
            int idx = auth.indexOf(' ');
            String scheme = (idx > 0) ? auth.substring(0, idx) : auth;
            meta.put("http.auth.scheme", scheme);
        }

        String contentType = req.getContentType();
        if (contentType != null) {
            meta.put("http.contentType", contentType);
        }

        String requestId = req.getHeader("X-Request-Id");
        if (requestId != null && !requestId.isBlank()) {
            meta.put("http.requestId", requestId);
        }

        return Pair.of(method + " " + uri, meta);
    }

    public <T> InboundEnvelope<T> fromHttp(
            InboundMetaExtractor<HttpServletRequest> metaExtractor, @NonNull HttpServletRequest req, T body) {

        Pair<String, Map<String, String>> sourceIdAndMeta = getHttpSourceIdWithMeta(req);
        Map<String, String> meta = sourceIdAndMeta.getRight();
        Map<String, String> extra = metaExtractor.extract(req);

        extra.forEach(meta::putIfAbsent);

        return fromSource(
                InboundEnvelope.InboundSource.HTTP,
                sourceIdAndMeta.getLeft(),
                Map.copyOf(meta),
                InboundSourceReader.identity(), body
        );
    }

    public <T> InboundEnvelope<T> fromHttp(
            @NonNull String sourceId, @NonNull Map<String, String> meta, T body) {
        return fromSource(
                InboundEnvelope.InboundSource.HTTP,
                sourceId,
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
