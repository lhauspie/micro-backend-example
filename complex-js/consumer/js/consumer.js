const express = require('express');
const ClientLibrary = require('../../business-logic-client-lib/js/js-js-business-logic-client-lib/client-lib');

const app = express();
const port = 3000;

const clientLibrary = new ClientLibrary('http://localhost:8080', 10);

app.use(express.json());

app.post('/execute', async (req, res) => {
    try {
        const objectToProcess = req.body;
        const result = await clientLibrary.executeBusinessLogic(objectToProcess);
        res.json({ result });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.listen(port, () => {
    console.log(`Consumer app listening at http://localhost:${port}`);
});
