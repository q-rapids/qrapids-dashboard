function drawChart() {
    for (i = 0; i < titles.length; ++i) {
        var angles = weights[i].map(x => x * 2 * Math.PI);
        var a = document.createElement('a');
        var t = titles[i].split(": &nbsp;");
        var title = t[0] + "<br/>" + t[1];
        if (title.indexOf('<') > -1)
            title = title.substr(0, title.indexOf('<'));
        if (isdsi) { //if it is a polar chart for Detailed Strategic Indicators
            var urlLink = "../QualityFactors/CurrentChart" + "?id=" + ids[i] + "&name=" + title;
        } else { //if it is a polar chart for Quality Factors
            var name = getParameterByName('si');
            var id = getParameterByName('siid');
            if (name.length != 0) //if we know from which Detailed Strategic Indicator we are coming
                var urlLink = "../Metrics/CurrentChart" + metRepresentationMode + "?id=" + ids[i] + "&si=" + name + "&siid=" + id + "&name=" + title;
            else
                var urlLink = "../Metrics/CurrentChart" + metRepresentationMode + "?id=" + ids[i] + "&name=" + title;
        }
        a.setAttribute("href", urlLink);
        a.innerHTML = t[0] + "<br/>" + t[1];
        a.style.fontSize = "16px";
        var div = document.createElement('div');
        div.id = t[0] + "<br/>" + t[1];
        div.style.display = "inline-block";
        div.style.margin = "0px 5px 60px 5px";
        var p = document.createElement('p');
        var ctx = document.createElement('canvas');
        ctx.id = 'canvas' + i;
        ctx.width = 400;
        ctx.style.display = "inline";
        document.getElementById("polarChart").appendChild(div).appendChild(ctx);
        div.appendChild(p).appendChild(a);
        ctx.getContext("2d");
        var dataset = [];
        // TODO dataset backgroundColor a param = 0.0 -> no fill
        dataset.push({ // data
            label: titles[i],
            xLabel: weights[i], // only used to have weights info on tooltip
            backgroundColor: colorsForPolar[i],
            borderColor: colorsForPolar[i],
            data: assessmentValues[i],
            fill: false
        });
        // TODO categories come diferent in Stacked Data parse
        console.log(categoriesForPolar);
        for (var k = categoriesForPolar.length-1; k >= 0; --k) {
            var fill = categoriesForPolar.length-1-k;
            if (k == categoriesForPolar.length-1) fill = true;
            // TODO  categories dataset backgroundColor a param = 0.0 -> no fill
            dataset.push({
                label: categoriesForPolar[k].name,
                borderWidth: 2,
                backgroundColor: hexToRgbA(categoriesForPolar[k].color, 0.0),
                borderColor: hexToRgbA(categoriesForPolar[k].color, 1),
                pointHitRadius: 0,
                pointHoverRadius: 0,
                pointRadius: 0,
                pointBorderWidth: 0,
                pointBackgroundColor: 'rgba(0, 0, 0, 0)',
                pointBorderColor: 'rgba(0, 0, 0, 0)',
                data: [].fill.call({ length: labels[i].length }, categoriesForPolar[k].upperThreshold),
                fill: fill
            })
        }

        console.log("dataset before make a chart");
        console.log(dataset);

        console.log("labels");
        console.log(labels);

        console.log("angles");
        console.log(angles);

        var t = titles[i].split(": &nbsp;");

        window.myLine = new Chart(ctx, {    //draw chart with the following config
            type: 'polarArea',
            data: {
                labels: labels[i],
                datasets: dataset
            },
            options: {
                "elements": {
                    "arc": {
                        "angle": angles,
                    }
                },
                title: {
                    display: false,
                    fontSize: 16,
                    text: t[0] + "<br/>" + t[1],
                },
                responsive: false,
                legend: {
                    position: 'top',
                    display: true
                },
                //maintainAspectRatio: true,
                aspectRatio: 1.8,
                scale: {    //make y axis scale 0 to 1 and set maximum number of axis lines
                    ticks: {
                        min: 0,
                        max: 1,
                        stepSize: 0.2,
                    }
                },
                tooltips: {
                    filter: function (tooltipItem) {
                        return tooltipItem.datasetIndex === 0;
                    },
                    callbacks: {
                        label: function (tooltipItem, data) {
                            var label = data.labels[tooltipItem.index] || '';

                            if (label) {
                                label += ': ';
                            }
                            label += Math.round(tooltipItem.yLabel * 100) / 100; // add assessment value
                            label += " (" + (data.datasets[0].xLabel[tooltipItem.index] * 100).toFixed(0) + "%)"; // add weight value
                            return label;
                        },
                        title: function (tooltipItem, data) {
                            if (tooltipItem.length != 0) {
                                var title = data.datasets[0].label.split(": &nbsp;");
                                return title[0] + ": " + title[1];
                            }
                        }
                    }
                }
            }
        });
        //Warnings
        if (typeof warnings !== "undefined") {
            var text = "";
            warnings[i].forEach(function (message) {
                if (text !== "") {
                    text += "\n"
                }
                text += message;
            });

            if (text !== "") {
                addWarning(div, text);
            }
        }
    }
}

function addWarning(div, message) {
    var warning = document.createElement("span");
    warning.setAttribute("class", "glyphicon glyphicon-alert");
    warning.title = message;
    warning.style.paddingLeft = "1em";
    warning.style.fontSize = "15px";
    warning.style.color = "yellow";
    warning.style.textShadow = "-2px 0 2px black, 0 2px 2px black, 2px 0 2px black, 0 -2px 2px black";
    div.append(warning);
}

function hexToRgbA(hex,a=1){ // (hex color, opacity)
    var c;
    if(/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)){
        c= hex.substring(1).split('');
        if(c.length== 3){
            c= [c[0], c[0], c[1], c[1], c[2], c[2]];
        }
        c= '0x'+c.join('');
        return 'rgba('+[(c>>16)&255, (c>>8)&255, c&255].join(',')+','+ a +')';
    }
    throw new Error('Bad Hex');
}

window.onload = function() {
    getData();
};