function createface(){
    $("#createface").css("display","block");
}
function closecreateface(){
    $("#createface").css("display","none");
}
function registerface(){
    $("#registerface").css("display","block");
}
function closeregisterface(){
    $("#registerface").css("display","none");
}
function deleteface(){
    $("#deleteface").css("display","block");
}
function closedeleteface(){
    $("#deleteface").css("display","none");
}
function searchface(){
    $("#searchface").css("display","block");
}
function closesearchface(){
    $("#searchface").css("display","none");
}
function uploadcreate(){
    $("#createface").css("display","none");
    var formData = new FormData();
    formData.append("dbname",$("#dbname").val());
    $.ajax({
        url: '/image/createfacedb',
        type:'POST',
        processData: false,
        contentType: false,
        dataType: "text",
        data: formData,
        success: function(data){
            alert("人脸库创建成功");
        },
        error: function(data){
            console(data);
        },
    })
}

function uploadregister(){
    $("#registerface").css("display","none");
    var formData = new FormData();
    var file=$("#imagefile")[0].files[0];
    formData.append("name",$("#name").val());
    formData.append("imagefile",file);
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
    formData.append("deldbname",$("#deldbname").val());
    $.ajax({
        url: '/image/registerface',
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
    formData.append("image",file);
    $.ajax({
        url: '/image/predict',
        type:'POST',
        processData: false,
        contentType: false,
        dataType:"text",
        data: formData,
        success: function(data){
            alert("你输入的人脸最有可能是："+data);
        },
        error:function(err){
            alert(err);
        }
    })
}
function test(){
    $("#lists").append("co11111");
}