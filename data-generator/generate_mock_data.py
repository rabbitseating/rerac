"""Generate demo RERAC risk data and insert it into MySQL.

This is a reconstructed portfolio/demo generator based on the documented original
pipeline. The original Python generator source was no longer available.
"""

import argparse
import os
import random
from datetime import date, datetime, timedelta

import mysql.connector

LOCATIONS = {
    "Blk8": ("blk8", "ttc8"),
    "Blk23": ("blk23", "ttc23"),
    "Blk51": ("blk51", "ttc51"),
    "Blk72": ("blk72", "ttc72"),
    "Blk73": ("blk73", "ttc73"),
    "BlkSIT": ("blkSIT", "ttcSIT"),
}


def database_connection():
    return mysql.connector.connect(
        host=os.getenv("DB_HOST", "localhost"),
        user=os.getenv("DB_USER", "root"),
        password=os.getenv("DB_PASSWORD", ""),
        database=os.getenv("DB_NAME", "testing"),
    )


def risk_value():
    return random.randint(0, 10)


def ttc_value():
    return round(random.uniform(0.5, 6.0), 2)


def insert_live_rows(cursor, rows: int):
    query = """
        INSERT INTO totalrisk (
            blk8, ttc8, blk23, ttc23, blk51, ttc51,
            blk72, ttc72, blk73, ttc73, blkSIT, ttcSIT, time_val
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """

    for offset in range(rows):
        timestamp = datetime.now() - timedelta(minutes=(rows - offset - 1) * 5)
        values = []
        for _ in LOCATIONS.values():
            values.extend([risk_value(), ttc_value()])
        values.append(timestamp.time().replace(microsecond=0))
        cursor.execute(query, values)


def insert_historical_rows(cursor, days: int):
    query = """
        INSERT INTO hml (location, avgrisk, hour, date)
        VALUES (%s, %s, %s, %s)
    """

    start_date = date.today() - timedelta(days=days - 1)
    for day_offset in range(days):
        current_date = start_date + timedelta(days=day_offset)
        for location in LOCATIONS:
            for hour in range(7, 20):
                cursor.execute(
                    query,
                    (location, round(random.uniform(0.0, 10.0), 2), hour, current_date),
                )


def main():
    parser = argparse.ArgumentParser(description="Generate RERAC demo risk data")
    parser.add_argument("--rows", type=int, default=12, help="live totalrisk rows")
    parser.add_argument("--days", type=int, default=1, help="historical chart days")
    args = parser.parse_args()

    connection = database_connection()
    try:
        cursor = connection.cursor()
        insert_live_rows(cursor, max(1, args.rows))
        insert_historical_rows(cursor, max(1, args.days))
        connection.commit()
        print(
            f"Inserted {max(1, args.rows)} live rows and "
            f"{max(1, args.days)} day(s) of chart data."
        )
    finally:
        connection.close()


if __name__ == "__main__":
    main()
