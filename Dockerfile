FROM sbtscala/scala-sbt:eclipse-temurin-21.0.8_9_1.12.3_3.3.7 AS builder
COPY . /lambda/src/
WORKDIR /lambda/src/
RUN sbt assembly

FROM public.ecr.aws/lambda/java:21
RUN dnf install -y rpm-4.16.1.3-29.amzn2023.0.10 rpm-libs-4.16.1.3-29.amzn2023.0.10 --releasever=latest && dnf clean all
COPY --from=builder /lambda/src/target/function.jar ${LAMBDA_TASK_ROOT}/lib/
CMD ["uk.gov.nationalarchives.tre.LambdaHandler::handleRequest"]
