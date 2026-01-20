package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Shop") })
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { /* Handle Cart Action */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go to Cart")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Shop for your pet!",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(16.dp))

            // Example Item Card
            ItemCard(name = "Pet Food", price = 10, onClick = { /* Handle Item Purchase */ })
            ItemCard(name = "Toy", price = 5, onClick = { /* Handle Item Purchase */ })
            ItemCard(name = "Accessory", price = 7, onClick = { /* Handle Item Purchase */ })
        }
    }
}

@Composable
fun ItemCard(name: String, price: Int, onClick: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for the item image
            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null)

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("\$$price", style = MaterialTheme.typography.bodyMedium)
            }

            // You could also add an "Add to Cart" button or just handle the click directly
        }
    }
}
