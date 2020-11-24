function drawChart() {
    for (i = 0; i < titles.length; ++i) {
        var a = document.createElement('a');
        var title = titles[i];
        if (titles[i].indexOf('<') > -1)
            title = titles[i].substr(0, titles[i].indexOf('<'));
        if (isdsi) { //if it is a radar chart for Detailed Strategic Indicators
            var urlLink = "../DetailedQualityFactors/CurrentChart" + DQFRepresentationMode+ "?id=" + ids[i] + "&name=" + title;
        } else { //if it is a radar chart for Quality Factors
            var name = getParameterByName('name');
            var id = getParameterByName('id');
            if (name.length != 0) //if we know from which Detailed Strategic Indicator we are coming
                var urlLink = "../Metrics/CurrentChart?id=" + ids[i] + "&si=" + name + "&siid=" + id + "&name=" + title;
            else
                var urlLink = "../Metrics/CurrentChart?id=" + ids[i] + "&name=" + title;
        }
        a.setAttribute("href", urlLink);
        a.innerHTML = titles[i];
        a.style.fontSize = "16px";
        var div = document.createElement('div');
        div.id = titles[i];
        div.style.display = "inline-block";
        div.style.margin = "0px 5px 60px 5px";
        var p = document.createElement('p');
        var ctx = document.createElement('canvas');
        ctx.id = 'canvas' + i;
        ctx.width = 400;
        ctx.style.display = "inline";
        document.getElementById("barChart").appendChild(div).appendChild(ctx);
        div.appendChild(p).appendChild(a);
        ctx.getContext("2d");
        /* TODO make triangle chart
        if (labels[i].length === 2) {
            labels[i].push(null);
        } else if (labels[i].length === 1) {
            labels[i].push(null);
            labels[i].push(null);
        }
        */
        var dataset = [];
        var t = titles[i].split("<br/>");
        console.log(values[i]);
        console.log(Array(values[i].length).fill('rgba(1, 119, 166, 0.0)'));
        dataset.push({ // data
            label: "Assessment value",
            backgroundColor: Array(values[i].length).fill('rgb(1, 119, 166)'),
            borderColor: Array(values[i].length).fill('rgb(1, 119, 166)'),
            //pointBackgroundColor: 'rgb(1, 119, 166)',
            //pointBorderColor: 'rgb(1, 119, 166)',
            categoryPercentage: 1.0,
            barPercentage: 0.5,
            barThickness: 3,
            //maxBarThickness: 8,
            //minBarLength: 2,
            data: values[i]
        });
        console.log(categories);
        /*
        for (var k = categories.length-1; k >= 0; --k) {
            var fill = categories.length-1-k;
            if (k == categories.length-1) fill = true;
            // TODO  categories dataset backgroundColor a param = 0.0 -> no fill
            dataset.push({
                label: categories[k].name,
                borderWidth: 1,
                backgroundColor: hexToRgbA(categories[k].color, 0.3),
                borderColor: hexToRgbA(categories[k].color, 0.3),
                pointHitRadius: 0,
                pointHoverRadius: 0,
                pointRadius: 0,
                pointBorderWidth: 0,
                pointBackgroundColor: 'rgba(0, 0, 0, 0)',
                pointBorderColor: 'rgba(0, 0, 0, 0)',
                data: [].fill.call({ length: labels[i].length }, categories[k].upperThreshold),
                fill: fill
            })
        }
        */
        console.log("dataset before make a chart");
        console.log(dataset);

        console.log("labels");
        console.log(labels[i]);

        // make labels in several lines
        var l = [];
        for (j = 0; j < labels[i].length; ++j) {
            l.push(labels[i][j].split(" "))
        }
        console.log(l);


        window.myLine = new Chart(ctx, {    //draw chart with the following config
            type: 'bar',
            data: {
                labels: l,
                datasets: dataset
            },
            options: {
                title: {
                    display: false,
                    fontSize: 16,
                    text: titles[i]
                },
                responsive: false,
                legend: {
                    position: 'top',
                    display: false
                },
                //maintainAspectRatio: true,
                //aspectRatio: 1.8,
                scales: {
                    /*xAxes: [{
                        ticks: {
                            maxRotation: 90,
                            minRotation: 80
                        }
                    }],*/
                    yAxes: [{
                        ticks: {
                            beginAtZero: true
                        }
                    }]
                },
                tooltips: {
                    filter: function (tooltipItem) {
                        return tooltipItem.datasetIndex === 0;
                    },
                    callbacks: {
                        label: function(tooltipItem, data) {
                            // get the data label and data value to display
                            var dataLabel = data.labels[tooltipItem.index].join(" ");
                            var value = ': ' + data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index].toLocaleString();
                            // return the text to display on the tooltip
                            return dataLabel + value;
                        },
                        title: function(tooltipItems, data) {
                            // Return value for title
                            //return data.labels[tooltipItems[0].index].join(" ");
                            return "";
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