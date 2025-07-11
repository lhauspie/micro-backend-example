"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const client_lib_1 = __importDefault(require("../../../../business-logic-client-lib/ts/js-ts-business-logic-client-lib/dist/client-lib"));
const router = express_1.default.Router();
router.post('/', (req, res) => __awaiter(void 0, void 0, void 0, function* () {
    const objectToExecute = req.body;
    try {
        const clientLibrary = new client_lib_1.default("http://localhost:8080", 10);
        const result = yield clientLibrary.executeBusinessLogic(objectToExecute);
        res.json(result);
        console.info("Business Logic executed successfully");
    }
    catch (error) {
        res.status(500).json({ error: error.message });
    }
}));
exports.default = router;
