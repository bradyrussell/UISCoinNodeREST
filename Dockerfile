FROM ubuntu:latest
LABEL author=BradyRussell
RUN apt-get -y update && apt-get -y upgrade && apt-get install -y openjdk-14-jre-headless wget
EXPOSE 8080/tcp
CMD wget -qO- https://github.com/bradyrussell/UISCoinNodeREST/raw/master/latest_release.txt | xargs wget -O api.jar && java -jar api.jar