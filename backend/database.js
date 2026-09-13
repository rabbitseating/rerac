const express = require('express');
const { createPool } = require('mysql2');

const app = express();
const PORT = Number(process.env.PORT || 3000);

const pool = createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'testing',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

app.get('/health', (req, res) => {
  pool.query('SELECT 1 AS ok', (err) => {
    if (err) {
      console.error('Database health check failed:', err.message);
      return res.status(503).json({ status: 'unhealthy', database: false });
    }
    return res.json({ status: 'ok', database: true });
  });
});

app.get('/risks', (req, res) => {
  const query = `
    SELECT blk8, ttc8, blk23, ttc23, blk51, ttc51,
           blk72, ttc72, blk73, ttc73, blkSIT, ttcSIT
    FROM totalrisk
    ORDER BY id DESC
    LIMIT 1
  `;

  pool.query(query, (err, results) => {
    if (err) return sendDatabaseError(res, err);
    return res.json(results);
  });
});

app.get('/last5risks', (req, res) => {
  const query = `
    SELECT blk8, blk23, blk51, blk72, blk73, blkSIT, time_val
    FROM totalrisk
    ORDER BY id DESC
    LIMIT 5
  `;

  pool.query(query, (err, results) => {
    if (err) return sendDatabaseError(res, err);
    return res.json(results);
  });
});

const locationByRoute = {
  risk8: 'Blk8',
  risk23: 'Blk23',
  risk51: 'Blk51',
  risk72: 'Blk72',
  risk73: 'Blk73',
  riskSIT: 'BlkSIT'
};

Object.entries(locationByRoute).forEach(([route, location]) => {
  app.get(`/${route}`, (req, res) => {
    const query = `
      SELECT avgrisk, hour, date
      FROM hml
      WHERE location = ?
        AND date = (SELECT MAX(date) FROM hml WHERE location = ?)
      ORDER BY hour ASC
    `;

    pool.query(query, [location, location], (err, results) => {
      if (err) return sendDatabaseError(res, err);
      return res.json(results);
    });
  });
});

function sendDatabaseError(res, err) {
  console.error('Database query failed:', err.message);
  return res.status(500).json({ error: 'Internal Server Error' });
}

app.use((req, res) => {
  res.status(404).json({ error: 'Not Found' });
});

app.listen(PORT, () => {
  console.log(`RERAC API listening on port ${PORT}`);
});
