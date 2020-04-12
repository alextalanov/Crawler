FROM docker.io/dockeralexandrtalan/sbt
ARG HOME=/root/app
COPY projects/Crawler $HOME
WORKDIR $HOME
ENV SBT_OPTS="-Xmx524m -XX:+UseConcMarkSweepGC
ENTRYPOINT sbt