const express = require('express');
const app = express();
const { createPool } = require('mysql2');

const pool = createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'testing'
});
//getting the lastest risks
app.get('/risks', (req, res) => {
  try {
    pool.query(`SELECT blk8, ttc8, blk23, ttc23, blk51, ttc51, blk72, ttc72, blk73, ttc73, blkSIT, ttcSIT FROM totalrisk ORDER BY id DESC LIMIT 1`, (err, results) => {
      if (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal Server Error' });
        return;
      }
      const risks = results.map(result => ({
        blk8: result.blk8,
        ttc8: result.ttc8,
        blk23: result.blk23,
        ttc23: result.ttc23,
        blkSIT: result.blkSIT,
        ttcSIT: result.ttcSIT,
        blk51: result.blk51,
        ttc51: result.ttc51,
        blk72: result.blk72,
        ttc72: result.ttc72,
        blk73: result.blk73,
        ttc73: result.ttc73,
      }));
      console.log(risks);
      res.json(risks);
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});
//getting the last 5 risk datas
app.get('/last5risks', (req, res) => {
  try {
    pool.query(`SELECT blk8, blk23, blk72, blk73, blkSIT, blk51, time_val FROM totalrisk ORDER BY id DESC LIMIT 5`, (err, results) => {
      if (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal Server Error' });
        return;
      }
      const last5Risks = results.map(result => ({
        blk8: result.blk8,
        blk23: result.blk23,
        blk51: result.blk51,
        blk72: result.blk72,
        blk73: result.blk73,
        blkSIT: result.blkSIT,
        time_val: result.time_val
      }));
      console.log(last5Risks);
      res.json(last5Risks);
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

//getting risk 8 data
app.get('/risk8', (req, res) => {
  try {
    pool.query(`SELECT * FROM hml WHERE location = 'Blk8'`, (err, results) => {
      if (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal Server Error' });
        return;
      }
      const blk8_ = results.map(result => ({
        avgrisk: result.avgrisk,
        hour: result.hour,
        date: result.date
      }));
      console.log(blk8_);
      res.json(blk8_);
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

//getting risk 23 data
app.get('/risk23', (req, res) => {
  try {
    pool.query(`SELECT * FROM hml WHERE location = 'Blk23'`, (err, results) => {
      if (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal Server Error' });
        return;
      }
      const blk23_ = results.map(result => ({
        avgrisk: result.avgrisk,
        hour: result.hour,
        date: result.date
      }));
      console.log(blk23_);
      res.json(blk23_);
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

//getting risk51 data
app.get('/risk51', (req, res) => {
  try {
    pool.query(`SELECT * FROM hml WHERE location = 'Blk51'`, (err, results) => {
      if (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal Server Error' });
        return;
      }
      const blk51_ = results.map(result => ({
        avgrisk: result.avgrisk,
        hour: result.hour,
        date: result.date
      }));
      console.log(blk51_);
      res.json(blk51_);
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

//getting risk72 data
app.get('/risk72', (req, res) => {
  try {
    pool.query(`SELECT * FROM hml WHERE location = 'Blk72'`, (err, results) => {
      if (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal Server Error' });
        return;
      }
      const blk72_ = results.map(result => ({
        avgrisk: result.avgrisk,
        hour: result.hour,
        date: result.date
      }));
      console.log(blk72_);
      res.json(blk72_);
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

//getting risk73 data
app.get('/risk73', (req, res) => {
  try {
    pool.query(`SELECT * FROM hml WHERE location = 'Blk73';`, (err, results) => {
      if (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal Server Error' });
        return;
      }
      const blk73_ = results.map(result => ({
        avgrisk: result.avgrisk,
        hour: result.hour,
        date: result.date
      }));
      console.log(blk73_);
      res.json(blk73_);
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

//getting SIT data
app.get('/riskSIT', (req, res) => {
  try {
    pool.query(`SELECT * FROM hml WHERE location = 'BlkSIT';`, (err, results) => {
      if (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal Server Error' });
        return;
      }
      const blkSIT_ = results.map(result => ({
        avgrisk: result.avgrisk,
        hour: result.hour,
        date: result.date
      }));
      console.log(blkSIT_);
      res.json(blkSIT_);
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server listening on port ${PORT}`);
});
// app.get('/risks', (req, res) => {
//   try {
//     pool.query(`SELECT date, time, risk8, ttc8, risk23, ttc23, risk51, ttc51, risk72, ttc72, risk73, ttc73, riskSIT, ttcSIT FROM risks`, (err, results) => {
//       if (err) {
//         console.error(err);
//         res.status(500).json({ error: 'Internal Server Error' });
//         return;
//       }
//
//         let sum8 =0;
//         let sum23 =0;
//         let sum51 =0;
//         let sum72 =0;
//         let sum73 =0;
//         let sumSIT =0;
//         //ttc remove when database changes
//         let sum8ttc =0;
//         let sum23ttc =0;
//         let sum51ttc =0;
//         let sum72ttc =0;
//         let sum73ttc =0;
//         let sumSITttc =0;

//         const risks = results.map(result => {
//         const formattedDate = formatDate(result.date);
//         const formattedTime = formatTime(result.time);
    
//         sum8 += result.risk8;
//         sum23 += result.risk23;
//         sum51 += result.risk51;
//         sum72 += result.risk72;
//         sum73 += result.risk73;
//         sumSIT += result.riskSIT;
//         //remove when database changes
//         sum8ttc += result.ttc8;
//         sum23ttc += result.ttc23;
//         sum51ttc += result.ttc51;
//         sum72ttc += result.ttc72;
//         sum73ttc += result.ttc73;
//         sumSITttc += result.ttcSIT;

//         return {
//           date: formattedDate,
//           time: formattedTime,
//           risk8: result.risk8,
//           ttc8: result.ttc8,
//           risk23: result.risk23,
//           ttc23: result.ttc23,
//           risk51: result.risk51,
//           ttc51: result.ttc51,
//           risk72: result.risk72,
//           ttc72: result.ttc72,
//           risk73: result.risk73,
//           ttc73: result.ttc73,
//           riskSIT: result.riskSIT,
//           ttcSIT: result.ttcSIT
//         };
//       });

//       const average8 = (sum8/results.length).toFixed(2);
//       const average23 = (sum23/results.length).toFixed(2);
//       const average51 = (sum51/results.length).toFixed(2);
//       const average72 = (sum72/results.length).toFixed(2);
//       const average73 = (sum73/results.length).toFixed(2);
//       const averageSIT = (sumSIT/results.length).toFixed(2);
//       //remove when database changes
//       const average8ttc = (sum8ttc/results.length).toFixed(2);
//       const average23ttc = (sum23ttc/results.length).toFixed(2);
//       const average51ttc = (sum51ttc/results.length).toFixed(2);
//       const average72ttc = (sum72ttc/results.length).toFixed(2);
//       const average73ttc = (sum73ttc/results.length).toFixed(2);
//       const averageSITttc = (sumSITttc/results.length).toFixed(2);


//       const response ={
//         risks: risks,
//         averages: {
//             average8: average8,
//             average23: average23,
//             average51: average51,
//             average72: average72,
//             average73: average73,
//             averageSIT: averageSIT,
//             //remove when database changes
//             average8ttc: average8ttc,
//             average23ttc: average23ttc,
//             average51ttc: average51ttc,
//             average72ttc: average72ttc,
//             average73ttc: average73ttc,
//             averageSITttc: averageSITttc
//           }
//       };

//       res.json(response);
//     });
//   } catch (err) {
//     console.error(err);
//     res.status(500).json({ error: 'Internal Server Error' });
//   }
// });

// function formatDate(dateString) {
//   const date = new Date(dateString);
//   const year = date.getFullYear();
//   const month = String(date.getMonth() + 1).padStart(2, '0');
//   const day = String(date.getDate()).padStart(2, '0');
  
//   return `${day}-${month}-${year}`;
// }

// function formatTime(timeString) {
//   const date = new Date(`2000-01-01T${timeString}`);
//   const hours = String(date.getHours()).padStart(2, '0');
//   const minutes = String(date.getMinutes()).padStart(2, '0');
  
//   return `${hours}:${minutes}`;
// }

// app.listen(3000, () => {
//   console.log('Server listening on port 3000');
// });
