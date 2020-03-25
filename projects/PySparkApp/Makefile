.DEFAULT_GOAL := run

init:
	pipenv --three install
	pipenv shell

analyze:
	flake8 ./src

run_tests:
	pytest --cov=src test/jobs/

run:
	find . -name '__pycache__' | xargs rm -rf
	rm -f jobs.zip
	cd src/ && tar -cvzf ../jobs.zip jobs/
	$(SPARK_HOME)/bin/spark-submit \
		--packages org.apache.spark:spark-streaming-kafka-0-10_2.11:2.4.0,org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0 \
		--master yarn --deploy-mode client \
		--driver-memory 4g \
		--executor-memory 2g \
		--executor-cores 2 \
		--num-executors=2 \
		--conf spark.yarn.am.memory=512m \
		--conf spark.scheduler.mode=FAIR \
		--queue spark_app \
		--py-files jobs.zip \
		src/main.py --job $(JOB_NAME) --config $(CONFIG_PATH)