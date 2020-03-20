import os
import sys
import json
import argparse
import importlib
from pathlib import Path
from pyspark.sql import SparkSession

if os.path.exists('jobs.zip'):
    sys.path.insert(0, 'jobs.zip')


def parse_config(path: Path, default_path: Path = Path.cwd() / 'src/resources/default_config.json') -> dict:
    with default_path.open(mode='r', encoding='utf-8') as json_file:
        default_config: dict = json.loads(json_file.read())

    with path.open(mode='r', encoding='utf-8') as json_file:
        loaded_config = json.loads(json_file.read())

    default_config.update(loaded_config)

    return default_config


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Pyspark job arguments')

    parser.add_argument('--job', type=str, required=True, dest='job_name',
                        help='The name of the spark job you want to run')
    parser.add_argument('--config', type=str, required=True, dest='config',
                        help='Path to json config file')

    args = parser.parse_args()

    spark = SparkSession \
        .builder \
        .appName(args.job_name) \
        .master("local[*]") \
        .getOrCreate()

    job_module = importlib.import_module(f'jobs.{args.job_name}')

    job_config = parse_config(path=Path(args.config))[args.job_name]
    job_module.run(spark, job_config)
