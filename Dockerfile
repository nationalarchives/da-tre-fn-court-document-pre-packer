FROM sbtscala/scala-sbt:eclipse-temurin-24.0.1_9_1.12.3_3.8.1 AS builder
COPY . /lambda/src/
WORKDIR /lambda/src/
RUN sbt assembly

FROM public.ecr.aws/lambda/java:21
COPY --from=builder /lambda/src/target/function.jar ${LAMBDA_TASK_ROOT}/lib/
CMD ["uk.gov.nationalarchives.tre.LambdaHandler::handleRequest"]
