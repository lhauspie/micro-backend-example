// business-logic.js

const input_example = {
    fence: {
        fence_length: 100, // in meters
        nb_segments: 3,
        nb_segments_greater_than_roll_length: 1,
    },
    materials: {
        wire_mesh_roll_length: 30, // in meters
        wire_mesh_roll_height: 1.80, // in meters
        tension_wire_roll_length: 50 // in meters
    }
};

const output_example = {
    result: {
        nb_wire_mesh_rolls: 4, // rouleaux de grillage
        nb_stacks: 41, // piquets
        nb_struts: 8, // jambes de force
        nb_tension_bars: 8, // barres de tension
        nb_tensioners: 16, // tendeurs
        nb_tension_wire_rolls: 9, // rouleaux de fil de tension
        nb_staples: 1200, // agrafes
        nb_tension_wires: 4, // fils de tension
    },
    errors: [
        {
            code: 'MISSING_FIELD',
            path: '.fence',
            message: '`.fence` is missing'
        },
        {
            code: "MISSING_FIELD",
            path: ".materials",
            message: "`.materials` is missing"
        }
    ],
    warnings: [
        {
            code: 'NEW_VERSION_AVAILABLE',
            message: 'A new version of the calculator is available, please consider to migrate to v2'
        }
    ]
};

function uniqByKeepLast(a, key) {
    return [
        ...new Map(
            a.map(x => [key(x), x])
        ).values()
    ]
}

function missing(path) {
    return {
        code: `MISSING_FIELD`,
        path,
        message: `\`${path}\` is missing`
    };
}

function negative(path) {
    return {
        code: `NEGATIVE_FIELD`,
        path,
        message: `\`${path}\` cannot be negative`
    };
}

function numberFormat(path) {
    return {
        code: `NUMBER_FORMAT`,
        path,
        message: `\`${path}\` must be a number`
    };
}

function validateField(value, path) {
    let errors = [];
    if (value === null || value === undefined) {
        errors.push(missing(path));
    } else if (typeof value !== "number") {
        errors.push(numberFormat(path));
    }
    if (value < 0) {
        errors.push(negative(path));
    }
    return errors;
}

function validateBothNbSegments(fenceInput) {
    let errors = [
        ...validateField(fenceInput.nb_segments_greater_than_roll_length, ".fence.nb_segments_greater_than_roll_length"),
        ...validateField(fenceInput.nb_segments, ".fence.nb_segments")
    ];
    if (fenceInput.nb_segments_greater_than_roll_length > fenceInput.nb_segments) {
        errors.push({
            code: 'MORE_NB_SEGMENTS_GREATER_THAN_ROLL_LENGTH_THAN_NB_SEGMENTS',
            message: '`fence.nb_segments_greater_than_roll_length` is greater `fence.nb_segments`'
        });
    };
    return errors;
}

function calculateNbWireMeshRolls(input) {
    let errors = [
        ...validateField(input.fence.fence_length, ".fence.fence_length"),
        ...validateField(input.materials.wire_mesh_roll_length, ".materials.wire_mesh_roll_length")
    ];

    let result = null;
    if (errors.length === 0) {
        result = divideAndCeil(input.fence.fence_length, input.materials.wire_mesh_roll_length);
    }
    return {result, errors};
}

function calculateNbStacks(input) {
    let errors = [
        ...validateField(input.fence.fence_length, ".fence.fence_length")
    ];

    let result = null;
    if (errors.length === 0) {
        result = divideAndCeil(input.fence.fence_length, 2.5) + 1;
    }
    return {result, errors};
}


function calculateNbStruts(input) {
    let errors = validateBothNbSegments(input.fence);

    let result = null;
    if (errors.length === 0) {
        result = (input.fence.nb_segments_greater_than_roll_length + input.fence.nb_segments) * 2;
    }

    return {result, errors};
}

function calculateNbTensionBars(input) {
    let errors = validateBothNbSegments(input.fence);

    let result = null;
    if (errors.length === 0) {
        result = (input.fence.nb_segments_greater_than_roll_length + input.fence.nb_segments) * 2;
    }

    return {result, errors};
}

function calculateNbTensioners(input, nbTensionWires) {
    let errors = validateBothNbSegments(input.fence);

    let result = null;
    if (errors.length === 0) {
        result = (input.fence.nb_segments_greater_than_roll_length + input.fence.nb_segments) * Math.ceil(nbTensionWires);
    }

    return {result, errors};
}

function calculateNbTensionWires(input) {
    let errors = [
        ...validateField(input.materials.wire_mesh_roll_height, ".materials.wire_mesh_roll_height")
    ];

    let result = null;
    if (errors.length === 0) {
        result = Math.ceil(input.materials.wire_mesh_roll_height * 2);
    }
    return {result, errors};
}

function calculateNbTensionWireRolls(input, nbTensionWires) {
    let errors = [
        ...validateField(input.fence.fence_length, ".fence.fence_length"),
        ...validateField(input.materials.tension_wire_roll_length, ".materials.tension_wire_roll_length")
    ];

    let result = null;
    if (errors.length === 0) {
        result = divideAndCeil(input.fence.fence_length * 1.05 * Math.ceil(nbTensionWires), input.materials.tension_wire_roll_length);
    }
    return {result, errors};
}

function calculateNbStaples(input, nbTensionWires) {
    let errors = [
        ...validateField(input.fence.fence_length, ".fence.fence_length")
    ];

    let result = null;
    if (errors.length === 0) {
        result = input.fence.fence_length * Math.ceil(nbTensionWires) * 3;
    }
    return {result, errors};
}


function divideAndCeil(numerator, denominator) {
    return Math.ceil(numerator / denominator);
}


function execute(input) {
    const output = {
        result: {},
        errors: [],
        warnings: []
    };

    try {
        if (input.fence === null || input.fence === undefined) {
            output.errors.push(missing('.fence', 'FENCE'));
        }
        if (input.materials === null || input.materials === undefined) {
            output.errors.push(missing('.materials', 'MATERIALS'));
        }
        if (output.errors.length > 0) {
            return output;
        }

        var {result, errors} = calculateNbWireMeshRolls(input);
        output.errors.push(...errors);
        output.result.nb_wire_mesh_rolls = result;

        var {result, errors} = calculateNbStacks(input);
        output.errors.push(...errors);
        output.result.nb_stacks = result;

        var {result, errors} = calculateNbStruts(input);
        output.errors.push(...errors);
        output.result.nb_struts = result;

        var {result, errors} = calculateNbTensionBars(input);
        output.errors.push(...errors);
        output.result.nb_tension_bars = result;

        var {result, errors} = calculateNbTensionWires(input);
        output.errors.push(...errors);
        output.result.nb_tension_wires = result;

        var {result, errors} = calculateNbTensioners(input, output.result.nb_tension_wires);
        output.errors.push(...errors);
        output.result.nb_tensioners = result;

        var {result, errors} = calculateNbTensionWireRolls(input, output.result.nb_tension_wires);
        output.errors.push(...errors);
        output.result.nb_tension_wire_rolls = result;

        var {result, errors} = calculateNbStaples(input, output.result.nb_tension_wires);
        output.errors.push(...errors);
        output.result.nb_staples = result;
    } catch (error) {
        output.errors.push({
            code: 'INTERNAL_ERROR',
            message: error.message
        });
    }

    return cleanup(output);
}

function cleanup(output) {
    output.errors = uniqByKeepLast(output.errors, x => x.message);
    if (output.errors.length === 0) {
        delete output.errors;
    }
    if (output.warnings.length === 0) {
        delete output.warnings;
    }
    return output;
}

module.exports = {execute};
