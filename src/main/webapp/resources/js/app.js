$(document).ready(function () {
    $("#run-sql").click(function () {
        var sql = $("#sql-input").val();
        $.ajax({
            type: "POST",
            url: "/",
            dataType: "json",
            data: {
                sql: sql
            },
            success: function (data) {
                var fields = data.fields;
                var values = data.data;
                $("#result-tr").empty();
                for (var i = 0; i < fields.length; i++) {
                    $("#result-tr").append("<th>" + fields[i] + "</th>")
                }
                $("#result-body").empty();
                for (var i = 0; i < values.length; i++) {
                    $("#result-body").append("<tr>");
                    for (var j = 0; j < fields.length; j++) {
                        $("#result-body").append("<td>" + values[i][fields[j]] + "</td>")
                    }
                    $("#result-body").append("</tr>");
                }

            },
            error: function (data) {
                $('p.text-danger').html(data.responseText);
            }

        });

    });
});