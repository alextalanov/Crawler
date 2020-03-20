.DEFAULT_GOAL := run

init:
	pipenv --three install
	pipenv shell

analyze:
	flake8 ./src

run_tests:
	pytest --cov=src test/jobs/

# source `pipenv --venv`/bin/activate
# make run JOB_NAME=counties_review CONFIG_PATH="$(pwd)/src/resources/default_config.json"
run:
	find . -name '__pycache__' | xargs rm -rf
	rm -f jobs.zip
	cd src/ && zip -r ../jobs.zip jobs/
	python3 ./src/main.py --job $(JOB_NAME) --config $(CONFIG_PATH)