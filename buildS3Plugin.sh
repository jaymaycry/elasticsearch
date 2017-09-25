mkdir elasticsearch
cd ./plugins/repository-s3/
gradle assemble
cp ./build/distributions/repository-s3-5.1.2-SNAPSHOT.jar ../../elasticsearch/repository-s3-5.1.2.jar
cd ../../
curl -o repository-s3-5.1.2-custom.zip https://artifacts.elastic.co/downloads/elasticsearch-plugins/repository-s3/repository-s3-5.1.2.zip
zip -u repository-s3-5.1.2-custom.zip elasticsearch/repository-s3-5.1.2.jar
unzip -l repository-s3-5.1.2-custom.zip
rm -rf elasticsearch
echo "Created repository-s3-5.1.2-custom.zip. Move this zip to elasticsearch-cluster-setup to include in Dockerimage."