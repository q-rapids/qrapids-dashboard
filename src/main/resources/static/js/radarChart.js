function drawChart() {
    for (i = 0; i < titles.length; ++i) {
        var a = document.createElement('a');
        if (isdsi) { //if it is a radar chart for Detailed Strategic Indicators
            var urlLink = "../QualityFactors/CurrentChart?id=" + ids[i] + "&name=" + titles[i];
        } else { //if it is a radar chart for Quality Factors
            var name = getParameterByName('name');
            var id = getParameterByName('id');
            if (name.length != 0) //if we know from which Detailed Strategic Indicator we are coming
                var urlLink = "../Metrics/CurrentChart?id=" + ids[i] + "&si=" + name + "&siid=" + id + "&name=" + titles[i];
            else
                var urlLink = "../Metrics/CurrentChart?id=" + ids[i] + "&name=" + titles[i];
        }
        var from = getParameterByName('from');
        var to = getParameterByName('to');
        if ($('#datepickerFrom').length || (from.length != 0 && to.length != 0)) {
            if ($('#datepickerFrom').length)
                urlLink = urlLink + "&from=" + $('#datepickerFrom').val() + "&to=" + $('#datepickerTo').val();
            else
                urlLink = urlLink + "&from=" + from + "&to=" + to;
        }
        a.setAttribute("href", urlLink);
        a.innerHTML = titles[i];
        a.style.fontSize = "16px";
        var div = document.createElement('div');
        div.style.display = "inline-block";
        div.style.margin = "0px 5px 60px 5px";
        var p = document.createElement('p');
        var ctx = document.createElement('canvas');
        ctx.id = 'canvas' + i;
        ctx.width = 400;
        //ctx.height = 350;
        ctx.style.display = "inline";
        document.getElementById("radarChart").appendChild(div).appendChild(ctx);
        div.appendChild(p).appendChild(a);
        ctx.getContext("2d");
        window.myLine = new Chart(ctx, {    //draw chart with the following config
            type: 'radar',
            data: {
                labels: labels[i],
                datasets: [{
                    label: titles[i],
                    backgroundColor: 'rgba(1, 119, 166, 0.2)',
                    borderColor: 'rgb(1, 119, 166)',
                    data: values[i],
                    fill: true
                }]
            },
            options: {
                title: {
                    display: false,
                    fontSize: 16,
                    text: titles[i]
                },
                responsive: false,
                legend: {
                    display: false
                },
                scale: {    //make y axis scale 0 to 1 and set maximum number of axis lines
                    ticks: {
                        min: 0,
                        max: 1,
                        maxTicksLimit: 5
                    }
                }
            }
        });
    }
}

window.onload = function() {
    getData();
};
