FROM docker.io/dockeralexandrtalan/sbt
ARG HOME=/root/app
COPY projects/Crawler $HOME
WORKDIR $HOME
ENV SBT_OPTS="-Xmx724m -XX:+UseConcMarkSweepGC"
ENTRYPOINT sbt