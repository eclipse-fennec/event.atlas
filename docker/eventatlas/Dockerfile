ARG APP_HOME=/opt/eventatlas
ARG RUNTIME_DIR=${APP_HOME}/runtime
ARG LOG_DIR=${RUNTIME_DIR}/log
ARG TMPDIR=/tmp/eventatlas
ARG RUNTIME_JAR=${APP_HOME}/eventatlas.runtime_docker.jar

FROM eclipse-temurin:21-jre-alpine AS builder

ARG APP_HOME
ARG RUNTIME_DIR
ARG LOG_DIR
ARG TMPDIR
ARG RUNTIME_JAR

RUN set -eux; \
    mkdir -p "${APP_HOME}" "${LOG_DIR}" "${TMPDIR}"; \
    chown -R 65532:65532 "${APP_HOME}" "${TMPDIR}"; \
    chmod -R u+rwX,go-rwx "${LOG_DIR}" "${TMPDIR}"

COPY content/eventatlas.runtime_docker.jar "${RUNTIME_JAR}"
COPY content/runtime/ "${RUNTIME_DIR}/"

# The .keep placeholders exist only to keep the mount-point directories in git;
# the file provider would try (and fail) to parse them as XMI.
RUN set -eux; \
    mkdir -p "${RUNTIME_DIR}/mappings" "${RUNTIME_DIR}/profiles"; \
    find "${RUNTIME_DIR}" -name '.keep' -delete; \
    chown -R 65532:65532 "${APP_HOME}"

FROM gcr.io/distroless/java21-debian12:nonroot

ARG APP_HOME
ARG RUNTIME_DIR
ARG LOG_DIR
ARG TMPDIR
ARG RUNTIME_JAR

ENV APP_HOME=${APP_HOME} \
    RUNTIME_DIR=${RUNTIME_DIR} \
    LOG_DIR=${LOG_DIR} \
    TMPDIR=${TMPDIR} \
    RUNTIME_JAR=${RUNTIME_JAR}

COPY --from=builder ${APP_HOME} ${APP_HOME}
COPY --from=builder --chown=65532:65532 ${TMPDIR} ${TMPDIR}

EXPOSE 8080
WORKDIR ${APP_HOME}

ENTRYPOINT ["java", \
    "-Dgosh.args=--nointeractive", \
    "-Djava.io.tmpdir=/tmp/eventatlas", \
    "-jar", "/opt/eventatlas/eventatlas.runtime_docker.jar"]
