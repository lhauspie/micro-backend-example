import express, { Request, Response } from 'express';
import ClientLibrary from '../../../../business-logic-client-lib/ts/dist/client-lib';

const router = express.Router();

router.post('/', async (req: Request, res: Response) => {
    const objectToExecute = req.body;

    try {
        const clientLibrary = new ClientLibrary("http://localhost:8080", 10);
        const result = await clientLibrary.executeBusinessLogic(objectToExecute);
        res.json(result);
        console.info("Business Logic executed successfully")
    } catch (error: any) {
        res.status(500).json({ error: error.message });
    }
});

export default router;
