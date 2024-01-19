/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.EntreeMenuScreen
import com.example.lunchtray.ui.OrderViewModel
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen

enum class Screen(@StringRes val title: Int) {
    Start(R.string.app_name),
    EntreeMenu(R.string.choose_entree),
    SideDishMenu(R.string.choose_side_dish),
    AccompanimentMenu(R.string.choose_accompaniment),
    Checkout(R.string.order_checkout),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayAppBar (
    currentScreen: Screen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        title = { Text(stringResource(id = currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if(canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_button)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.valueOf(
        backStackEntry?.destination?.route ?: Screen.Start.name
    )
    // Create ViewModel
    val viewModel: OrderViewModel = viewModel()

    Scaffold(
        topBar = {
            LunchTrayAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = Screen.Start.name,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(route = Screen.Start.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = {
                        navController.navigate(Screen.EntreeMenu.name)
                    },
                    modifier = Modifier.fillMaxSize())
            }

            composable(route = Screen.EntreeMenu.name) {
                EntreeMenuScreen(
                    options = DataSource.entreeMenuItems,
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel,
                            navController,
                        )
                    },
                    onNextButtonClicked = { navController.navigate(Screen.SideDishMenu.name) },
                    onSelectionChanged = { viewModel.updateEntree(it) }
                )
            }

            composable(route = Screen.SideDishMenu.name) {
                SideDishMenuScreen(
                    options = DataSource.sideDishMenuItems,
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel,
                            navController,
                        )
                    },
                    onNextButtonClicked = { navController.navigate(Screen.AccompanimentMenu.name) },
                    onSelectionChanged = { viewModel.updateSideDish(it) }
                )
            }

            composable(route = Screen.AccompanimentMenu.name) {
                AccompanimentMenuScreen(
                    options = DataSource.accompanimentMenuItems,
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel,
                            navController,
                        )
                    },
                    onNextButtonClicked = { navController.navigate(Screen.Checkout.name) },
                    onSelectionChanged = { viewModel.updateAccompaniment(it) }
                )
            }
            
            composable(route = Screen.Checkout.name) {
                CheckoutScreen(
                    orderUiState = uiState,
                    onNextButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel,
                            navController,
                        )
                    },
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel,
                            navController,
                        )
                    })
            }
        }
    }
}

private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController,
) {
    viewModel.resetOrder()
    navController.popBackStack(Screen.Start.name, inclusive = false)
}
