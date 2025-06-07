package com.example.voyago.view

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voyago.viewmodel.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.voyago.R
import com.example.voyago.model.Trip


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorePage(navController: NavController, vm: TripViewModel = viewModel(factory = Factory)) {

    val filteredTrips by vm.filteredList.collectAsState()

    LaunchedEffect(
        vm.filterDestination,
        vm.filterMinPrice,
        vm.filterMaxPrice,
        vm.filterDuration,
        vm.filterGroupSize,
        vm.filtersTripType,
        vm.filterCompletedTrips,
        vm.filterBySeats
    ) {
        vm.updatePublishedTrip()
        vm.setMaxMinPrice()

        if(vm.userAction != TripViewModel.UserAction.SEARCHING && vm.userAction != TripViewModel.UserAction.VIEW_TRIP) {
            vm.resetFilters()
        }
        vm.applyFilters()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                //Filters Button
                Button(
                    onClick = {
                        vm.userAction = TripViewModel.UserAction.FILTER_SELECTION
                        navController.navigate("filters_selection")
                    },
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(10.dp),// Border with rounded corne
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(193, 165, 195), // Change background color
                        contentColor = Color.Black // Change text color
                    )
                ) {
                    Text("Filters")
                }
            }
        }

        //List of trips
        if (filteredTrips.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Text("No trips for the selected filters.")
                }
            }
        } else {
            items(filteredTrips) { trip ->
                TripCard(trip, navController, vm, false,false)
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun TripCard(
    trip: Trip,
    navController: NavController,
    vm: TripViewModel,
    edit: Boolean,
    isDraft: Boolean = false // 新增参数：是否为草稿状态，默认为 false
) {

    //Clicking on the card the user goes to the page that show the details of the trip
    // 点击卡片，用户进入显示行程详情的页面
    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 10.dp) // 设置卡片内边距
            .fillMaxWidth() // 填满可用宽度
            .height(200.dp), // 设置卡片高度为200dp
        shape = CardDefaults.elevatedShape, // 使用默认的凸起形状
        onClick = { // 设置点击事件
            vm.setSelectedTrip(trip) // 设置选中的行程
            vm.userAction = TripViewModel.UserAction.VIEW_TRIP // 设置用户操作为查看行程
            navController.navigate("trip_details") // 导航到行程详情页面
        }
    ) {
        Box { // 创建一个容器盒子
            if(!trip.photo.isUriString()) { // 如果照片不是URI格式
                //AsyncImage with resources.Drawable 使用资源文件的异步图片
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data( // 设置图片数据源
                            LocalContext.current.resources.getIdentifier(
                                trip.photo, // 图片名称
                                "drawable", // 资源类型为drawable
                                LocalContext.current.packageName // 当前应用包名
                            )
                        )
                        .crossfade(true) // 启用交叉淡入动画效果
                        .build(),
                    contentDescription = trip.destination, // 图片内容描述
                    contentScale = ContentScale.Crop, // 图片缩放方式为裁剪
                    modifier = Modifier
                        .fillMaxWidth() // 填满可用宽度
                        .height(200.dp), // 设置高度为200dp
                    colorFilter = if (isDraft) { // 根据是否为草稿状态应用颜色滤镜
                        androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                            androidx.compose.ui.graphics.ColorMatrix().apply {
                                setToSaturation(0f) // 设置饱和度为0，图片变为灰白色
                            }
                        )
                    } else null // 正常状态下不应用颜色滤镜
                )
            } else {
                //Async Image with Uri 使用URI的异步图片
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(trip.photo) // 使用URI作为图片数据源
                        .crossfade(true) // 启用交叉淡入动画效果
                        .build(),
                    contentDescription = "Selected Trip Photo", // 图片内容描述
                    contentScale = ContentScale.Crop, // 图片缩放方式为裁剪
                    modifier = Modifier.fillMaxSize(), // 填满整个容器
                    colorFilter = if (isDraft) { // 根据是否为草稿状态应用颜色滤镜
                        androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                            androidx.compose.ui.graphics.ColorMatrix().apply {
                                setToSaturation(0f) // 设置饱和度为0，图片变为灰白色
                            }
                        )
                    } else null // 正常状态下不应用颜色滤镜
                )
            }

            // 草稿状态指示器
            if (isDraft) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart) // 对齐到左上角
                        .padding(8.dp) // 设置外边距
                        .background(
                            color = Color(0xFFFF9800).copy(alpha = 0.9f), // 橙色半透明背景
                            shape = RoundedCornerShape(12.dp) // 圆角矩形形状
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp) // 设置内边距
                ) {
                    Text(
                        text = "DRAFT", // 显示"草稿"标识
                        color = Color.White, // 白色文字
                        fontWeight = FontWeight.Bold, // 粗体字
                        fontSize = 10.sp // 字体大小
                    )
                }
            }

            //Destination and Title information 目的地和标题信息
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart) // 对齐到左下角
                    .padding(vertical = 10.dp, horizontal = 10.dp) // 设置外边距
                    .background(
                        color = if (isDraft) {
                            Color(0xAA666666) // 草稿状态：使用更深的灰色背景
                        } else {
                            Color(0xAA444444) // 正常状态：使用深灰色背景
                        },
                        shape = MaterialTheme.shapes.small // 小圆角形状
                    )
            ) {
                Column(
                    modifier = Modifier.padding(10.dp), // 设置内边距
                    horizontalAlignment = Alignment.Start // 水平左对齐
                ) {
                    Text(
                        text = if (isDraft) "${trip.destination} (Copy)" else trip.destination, // 草稿状态添加"(Copy)"后缀
                        color = Color.White, // 白色文字
                        fontWeight = FontWeight.Bold // 粗体字
                    )
                    Text(
                        text = trip.title, // 显示行程标题
                        color = Color.White // 白色文字
                    )
                }
            }

            //If the trip can be edit 如果行程可以编辑
            if (edit) {
                //Edit button that send the user to the edit page 编辑按钮，点击跳转到编辑页面
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = 10.dp) // 设置外边距
                        .align(alignment = Alignment.TopEnd) // 对齐到右上角
                        .wrapContentSize() // 包裹内容大小
                        .background(
                            color = if (isDraft) {
                                Color(0xe6, 0xe0, 0xe9, 200) // 草稿状态：更透明的背景
                            } else {
                                Color(0xe6, 0xe0, 0xe9, 255) // 正常状态：不透明背景
                            },
                            shape = MaterialTheme.shapes.small // 小圆角形状
                        )
                ) {
                    val painterEdit = painterResource(R.drawable.edit) // 获取编辑图标资源
                    Image(
                        painter = painterEdit, // 设置图标画笔
                        contentDescription = "edit", // 图标内容描述
                        modifier = Modifier
                            .size(35.dp) // 设置图标大小
                            .clickable { // 设置点击事件
                                vm.editTrip = trip // 设置要编辑的行程
                                vm.userAction = TripViewModel.UserAction.EDIT_TRIP // 设置用户操作为编辑行程
                                navController.navigate("edit_trip") // 导航到编辑行程页面
                            },
                        colorFilter = if (isDraft) {
                            androidx.compose.ui.graphics.ColorFilter.tint(Color.Gray) // 草稿状态：图标变灰色
                        } else null // 正常状态：保持原色
                    )
                }
            }

            //If the trip has the max number of participants or it's already started
            // 如果行程已达到最大参与人数或已经开始
            if (trip.status == Trip.TripStatus.COMPLETED.toString()) {
                //Banner that indicated that the trip has already happened 显示行程已完成的横幅
                CompletedBanner(Modifier.align(Alignment.TopEnd)) // 对齐到右上角
            } else if (!trip.canJoin() && !isDraft) { // 非草稿状态且无法加入时显示横幅
                //Banner that shows that nobody can join the trip anymore 显示无法加入行程的横幅
                BookedBanner(Modifier.align(Alignment.TopEnd)) // 对齐到右上角
            }
        }
    }
}
