$(document).ready(function(){
    $("#imagefile").change(function (e) {
        var file = e.target.files||e.dataTransfer.files;
        if(file) {
            var reader = new FileReader();
            reader.onload = function () {
                $("#longimg img").attr("src", reader.result);

            }
            $("#longimg").css("display","block");
            $("#imgshow1").css("display","none");
            $("#imgshow2").css("display","none");
            reader.readAsDataURL(file[0]);
        }
    });

    $("#faceimage").change(function (e) {
        var file = e.target.files||e.dataTransfer.files;
        if(file) {
            var reader = new FileReader();
            reader.onload = function () {
                $("#longimg img").attr("src", reader.result);

            }
            $("#longimg").css("display","block");
            $("#imgshow1").css("display","none");
            $("#imgshow2").css("display","none");
            reader.readAsDataURL(file[0]);
        }
    });
    $("#face1").change(function (e) {
        var file = e.target.files||e.dataTransfer.files;
        if(file) {
            var reader = new FileReader();
            reader.onload = function () {
                $("#imgshow1 img").attr("src", reader.result);

            }
            $("#longimg").css("display","none");
            $("#imgshow1").css("display","block");
            $("#longimg img").attr("src","../img/leftbg.jpg");
            reader.readAsDataURL(file[0]);
        }
    });
    $("#face2").change(function (e) {
        var file = e.target.files||e.dataTransfer.files;
        if(file) {
            var reader = new FileReader();
            reader.onload = function () {
                $("#imgshow2 img").attr("src", reader.result);

            }
            $("#longimg").css("display","none");
            $("#imgshow2").css("display","block");
            $("#longimg img").attr("src","../img/leftbg.jpg");
            reader.readAsDataURL(file[0]);
        }
    });

});
function registerface(){
    $("#longimg img").attr("src","../img/leftbg.jpg");
    $("#registerface").css("display","block");
    $("#deleteface").css("display","none");
    $("#searchface").css("display", "none");
    $("#compareface").css("display", "none");
    $("#longimg").css("display","block");
    $("#imgshow2").css("display","none");
    $("#imgshow1").css("display","none");
    $("#l2 button").css("background","yellow")
    $("#l4 button").css("background","#6fa5db")
    $("#l5 button").css("background","#6fa5db")
    $("#l6 button").css("background","#6fa5db")
}
function closeregisterface(){
    $("#longimg img").attr("src","../img/leftbg.jpg");
    $("#registerface").css("display","none");
    $("#longimg").css("display","block");
    $("#imgshow2").css("display","none");
    $("#imgshow1").css("display","none");
    $("#l2 button").css("background","#6fa5db")
}
function deleteface(){
    $("#longimg img").attr("src","../img/leftbg.jpg");
    $("#deleteface").css("display","block");
    $("#searchface").css("display", "none");
    $("#compareface").css("display", "none");
    $("#registerface").css("display","none");
    $("#longimg").css("display","block");
    $("#imgshow2").css("display","none");
    $("#imgshow1").css("display","none");
    $("#l5 button").css("background","yellow")
    $("#l2 button").css("background","#6fa5db")
    $("#l4 button").css("background","#6fa5db")
    $("#l6 button").css("background","#6fa5db")
}
function closedeleteface(){
    $("#longimg img").attr("src","../img/leftbg.jpg");
    $("#deleteface").css("display","none");
    $("#longimg").css("display","block");
    $("#imgshow2").css("display","none");
    $("#imgshow1").css("display","none");
    $("#l5 button").css("background","#6fa5db")
}
function searchface(){
    $("#longimg img").attr("src","../img/leftbg.jpg");
    $("#searchface").css("display","block");
    $("#deleteface").css("display","none");
    $("#compareface").css("display", "none");
    $("#registerface").css("display","none");
    $("#longimg").css("display","block");
    $("#imgshow2").css("display","none");
    $("#imgshow1").css("display","none");
    $("#l4 button").css("background","yellow")
    $("#l2 button").css("background","#6fa5db")
    $("#l5 button").css("background","#6fa5db")
    $("#l6 button").css("background","#6fa5db")
}
function closesearchface() {
    $("#longimg img").attr("src","../img/leftbg.jpg");
    $("#searchface").css("display", "none");
    $("#longimg").css("display","block");
    $("#imgshow2").css("display","none");
    $("#imgshow1").css("display","none");
    $("#searchtext").text("人脸检索结果");
    $("#l4 button").css("background","#6fa5db")
}
function compareface() {
    $("#longimg img").attr("src","../img/leftbg.jpg");
    $("#compareface").css("display", "block");
    $("#searchface").css("display","none");
    $("#deleteface").css("display","none");
    $("#registerface").css("display","none");
    $("#longimg").css("display","block");
    $("#imgshow2").css("display","none");
    $("#imgshow1").css("display","none");
    $("#l4 button").css("background","#6fa5db")
    $("#l2 button").css("background","#6fa5db")
    $("#l5 button").css("background","#6fa5db")
    $("#l6 button").css("background","yellow")
}
function closecompareface() {
    $("#longimg img").attr("src","../img/leftbg.jpg");
    $("#compareface").css("display", "none");
    $("#longimg").css("display","block");
    $("#imgshow2").css("display","none");
    $("#imgshow1").css("display","none");
    $("#l6 button").css("background","#6fa5db")
}
function uploadregister(){
    $("#registerface").css("display","none");
    var formData = new FormData();
    var file=$("#imagefile")[0].files[0];
    formData.append("name",$("#name").val());
    formData.append("imagefile",file);
    formData.append("workunit",$("#workunit").val());
    formData.append("occupation",$("#occupation").val());
    formData.append(("sex"),$.trim($("#select").val()));

    $.ajax({
        url: '/image/registerface',
        type:'POST',
        processData: false,
        contentType: false,
        dataType:"text",
        data: formData,
        success: function(data){
                alert("注册成功");
        },
        error:function(err){
            alert("注册失败");
        }
    })
}
function deletefacedb(){
    $("#deleteface").css("display","none");
    var formData = new FormData();
    var file = $("#deleteimage")[0].files[0];
    formData.append("image",file);
    $.ajax({
        url: '/image/deleteface',
        type:'POST',
        processData: false,
        contentType: false,
        dataType:"text",
        data: formData,
        success: function(data){
            alert("删除成功");
        },
        error:function(err){
            alert("删除失败");
        }
    })
}
function searchfacedb(){
    var formData = new FormData();
    var file=$("#faceimage")[0].files[0];
    formData.append("imagefile",file);
    $.ajax({
        url: '/image/searchfaceimage',
        type:'POST',
        processData: false,
        contentType: false,
        dataType:"text",
        data: formData,
        success: function(data){
            alert(data);
            $("#searchtext").text("查找的人是 "+data);
        },
        error:function(err){
            alert(err);
        }
    })
}
function facescore(){
    var formData = new FormData();
    var file1=$("#face1")[0].files[0];
    var file2=$("#face2")[0].files[0];
    formData.append("imagefile",file1);
    formData.append("imagefile",file2);
    $.ajax({
        url: '/image/predict',
        type:'POST',
        processData: false,
        contentType: false,
        dataType:"text",
        data: formData,
        success: function(data){
            $("#comparetext").text("人脸比对得分: "+data);
        },
        error:function(err){
            alert(err);
        }
    })
}
