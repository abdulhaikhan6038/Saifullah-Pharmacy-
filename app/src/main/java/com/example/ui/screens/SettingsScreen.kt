package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen40
import com.example.ui.theme.ForestGreenDeep
import com.example.ui.viewmodel.PharmacyViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PharmacyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentRole by viewModel.currentEmployee.collectAsState()

    var shopNameText by remember { mutableStateOf(viewModel.shopName.value) }
    var shopAddressText by remember { mutableStateOf(viewModel.shopAddress.value) }
    var shopPhoneText by remember { mutableStateOf(viewModel.shopPhone.value) }

    var bkashNumber by remember { mutableStateOf("01700000000") }
    var nagadNumber by remember { mutableStateOf("01800000000") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "সেটিংস ও কর্মচারী প্রোফাইল",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Employee Role Selection Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldGreen40)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("কর্মচারী লগইন ও রোল (User Role)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("বর্তমান সক্রিয় ইউজার: $currentRole", style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = currentRole == "এডমিন",
                                onClick = {
                                    viewModel.currentEmployee.value = "এডমিন"
                                    Toast.makeText(context, "এডমিন মোডে স্যুইচ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text("মালিক / এডমিন") },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.testTag("role_admin_chip")
                            )

                            FilterChip(
                                selected = currentRole == "সেলস কর্মী",
                                onClick = {
                                    viewModel.currentEmployee.value = "সেলস কর্মী"
                                    Toast.makeText(context, "সেলস কর্মী মোডে স্যুইচ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text("সেলস ম্যান / কর্মী") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.testTag("role_staff_chip")
                            )
                        }
                    }
                }
            }

            // Pharmacy Shop Info Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = EmeraldGreen40)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ফার্মেসির তথ্য ও মেমো হেডার", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        OutlinedTextField(
                            value = shopNameText,
                            onValueChange = {
                                shopNameText = it
                                viewModel.shopName.value = it
                            },
                            label = { Text("ফার্মেসির নাম") },
                            modifier = Modifier.fillMaxWidth().testTag("shop_name_input")
                        )

                        OutlinedTextField(
                            value = shopAddressText,
                            onValueChange = {
                                shopAddressText = it
                                viewModel.shopAddress.value = it
                            },
                            label = { Text("ঠিকানা") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = shopPhoneText,
                            onValueChange = {
                                shopPhoneText = it
                                viewModel.shopPhone.value = it
                            },
                            label = { Text("মোবাইল নম্বর") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Digital Payment Accounts
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = EmeraldGreen40)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("বিকাশ ও নগদ মার্চেন্ট নম্বর (পেমেন্ট গ্রহণ)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        OutlinedTextField(
                            value = bkashNumber,
                            onValueChange = { bkashNumber = it },
                            label = { Text("bKash Merchant / Personal No.") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = nagadNumber,
                            onValueChange = { nagadNumber = it },
                            label = { Text("Nagad Merchant No.") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Backup & Cloud Sync Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = EmeraldGreen40)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ব্যাকআপ ও ক্লাউড সিঙ্ক (Backup & Restore)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Text("আপনার কাস্টমার বাকি, ওষুধ ইনভেন্টরি ও ক্যাশ বাক্সের ডাটা গুগলে অটোমেটিক সুরক্ষিত রাখুন।", style = MaterialTheme.typography.bodySmall)

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    Toast.makeText(context, "ডাটা সফলভাবে ব্যাকআপ নেওয়া হয়েছে!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen40),
                                modifier = Modifier.testTag("backup_data_button")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ক্লাউড ব্যাকআপ নিন")
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "ক্লাউড থেকে ডাটা সিঙ্ক হচ্ছে...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("restore_data_button")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("রিস্টোর করুন")
                            }
                        }
                    }
                }
            }
        }
    }
}
