import express, { Request, Response } from 'express';
import executeRouter from './routes/execute';

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.use('/execute', executeRouter);

app.use((err: Error, req: Request, res: Response) => {
    console.error(err.stack);
    res.status(500).send('Something broke!');
});

app.listen(PORT, () => {
    console.log(`Consumer app listening at http://localhost:${PORT}`);
});
