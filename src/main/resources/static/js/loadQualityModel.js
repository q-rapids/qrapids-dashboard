var url = parseURLSimple("../api/qualityModel");

var prj = "test";
//var prj = sessionStorage.getItem("prj");

function loadData() {
    jQuery.ajax({
        dataType: "json",
        type: "GET",
        url : url,
        async: true,
        success: function (data) {
            buildTree(data);
        }});
}

function buildTree(strategicIndicators) {
    var qmnodes = new Map();
    var qmedges = [];
    for (var i = 0; i < strategicIndicators.length; i++) {
        var strategicIndicator = strategicIndicators[i];
        // Create strategic indicator node
        var node = {
            data : {
                id : strategicIndicator.id,
                color : "green",
                label : strategicIndicator.id + " : " + parseValue(strategicIndicator.value)
            }
        };
        if (!qmnodes.has(strategicIndicator.id))
            qmnodes.set(strategicIndicator.id, node);

        for (var j = 0; j < strategicIndicator.factors.length; j++) {
            var factor = strategicIndicator.factors[j];
            // Create factor node
            var node = {
                data : {
                    id : factor.id,
                    color : "orange",
                    label : factor.id + " : " + parseValue(factor.value)
                }
            };
            if (!qmnodes.has(factor.id))
                qmnodes.set(factor.id, node);
            //Create relation factor -> strategic indicator
            var edge = {
                data: {
                    source: factor.id,
                    target: strategicIndicator.id,
                    weight : factor.weight
                }
            };
            qmedges.push( edge );

            for (var k = 0; k < factor.metrics.length; k++) {
                var metric = factor.metrics[k];
                // Create metric node
                var node = {
                    data : {
                        id : metric.id,
                        color : "blue",
                        label : metric.id + " : " + parseValue(metric.value)
                    }
                };
                if (!qmnodes.has(metric.id))
                    qmnodes.set(metric.id, node);
                //Create relation metric -> factor
                var edge = {
                    data: {
                        source: metric.id,
                        target: factor.id,
                        weight : metric.weight
                    }
                };
                qmedges.push( edge );
            }
        }
    }
    displayData(Array.from(qmnodes.values()), qmedges);
}

function parseValue(value) {
    if (isNumeric(value)) {
        value = parseFloat(value).toFixed(2);
    }
    return value;
}

function isNumeric(value) {
    return !isNaN(value);
}

function displayData(qmnodes, qmedges) {
    var cy = window.cy = cytoscape({
        container: document.getElementById('cy'),
        boxSelectionEnabled: false,
        autounselectify: true,
        layout: {
            name: 'dagre',
            rankDir : 'RL',
            ranker: 'longest-path'
        },
        style: [
            {
                selector: 'node',
                style: {
                    'color' : 'white',
                    'width' : 'label',
                    'height' : 'label',
                    'text-halign' : 'center',
                    'text-valign' : 'center',
                    'shape' : 'roundrectangle',
                    'content': 'data(label)',
                    'background-color': 'data(color)',
                    'padding' : '5px',
                    'border-style' : 'solid',
                    'border-color' : 'darkgray',
                    'border-width' : 1
                }
            },
            {
                selector: 'edge',
                style: {
                    'width': 2,
                    'target-arrow-shape': 'triangle',
                    'line-color': 'darkgray',
                    'target-arrow-color': 'darkgray',
                    'curve-style': 'bezier',
                    'label' : 'data(weight)',
                    'font-size' : '12px'
                }
            }
        ],
        elements: {
            nodes: qmnodes,
            edges: qmedges
        }
    });

    cy.on('click', 'node', function(evt){

    });

}

loadData();
